package com.example.quickvendadmin.fragments;

import com.example.quickvendadmin.Vendor;
import com.example.quickvendadmin.MenuItem;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.example.quickvendadmin.R;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.SetOptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class EditProfileFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 101;

    private ImageView profileImageView;
    private Button selectImageButton;
    private String encryptedImageString = null;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FusedLocationProviderClient fusedLocationClient;
    private Switch switchOnline;
    private TextView addressTextView;
    private EditText stallName, category, contactNumber;
    private LinearLayout menuItemsContainer;
    private Button saveButton, updateButton, addMenuItemButton;
    private String vendorId;

    private List<MenuItem> menuItems = new ArrayList<>();

    private boolean isImageChanged = false;
    private boolean isOnlineStatusPreserved = false;

    public EditProfileFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        vendorId = auth.getCurrentUser().getUid();

        stallName = rootView.findViewById(R.id.stall_name);
        category = rootView.findViewById(R.id.category);
        contactNumber = rootView.findViewById(R.id.contact_number);
        switchOnline = rootView.findViewById(R.id.switch_online);
        addressTextView = rootView.findViewById(R.id.address_textview);
        menuItemsContainer = rootView.findViewById(R.id.menu_items_container);
        saveButton = rootView.findViewById(R.id.save_button);
        updateButton = rootView.findViewById(R.id.update_button);
        addMenuItemButton = rootView.findViewById(R.id.add_menu_item_button);
        profileImageView = rootView.findViewById(R.id.profile_image_view);
        selectImageButton = rootView.findViewById(R.id.btn_select_image);

        selectImageButton.setOnClickListener(v -> openImagePicker());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

        addMenuItemButton.setOnClickListener(v -> addMenuItem());
        saveButton.setOnClickListener(v -> saveVendorProfile());
        updateButton.setOnClickListener(v -> saveVendorProfile());

        getVendorProfile();

        return rootView;
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                profileImageView.setImageBitmap(bitmap);
                encryptedImageString = encodeImageToBase64(bitmap);
                isImageChanged = true;
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.NO_WRAP);
    }

    private void getVendorProfile() {
        db.collection("vendors").document(vendorId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Vendor vendor = documentSnapshot.toObject(Vendor.class);
                        if (vendor != null) {
                            stallName.setText(vendor.getStallName());
                            category.setText(vendor.getCategory());
                            contactNumber.setText(vendor.getContactNumber());

                            boolean currentOnline = vendor.isOnline();
                            switchOnline.setChecked(currentOnline);
                            isOnlineStatusPreserved = currentOnline;

                            addressTextView.setText(vendor.getAddress());

                            if (vendor.getMenuItems() != null) {
                                menuItems = vendor.getMenuItems();
                            } else {
                                menuItems = new ArrayList<>();
                            }
                            displayMenuItems();

                            String base64Image = vendor.getProfileImageEncrypted();
                            if (base64Image != null && !base64Image.isEmpty()) {
                                byte[] decodedBytes = Base64.decode(base64Image, Base64.NO_WRAP);
                                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                                profileImageView.setImageBitmap(decodedBitmap);
                            }

                            Toast.makeText(getContext(), "Profile loaded", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error loading profile: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void displayMenuItems() {
        menuItemsContainer.removeAllViews();
        for (MenuItem item : menuItems) {
            View itemView = LayoutInflater.from(getContext()).inflate(R.layout.menu_item_view, menuItemsContainer, false);
            EditText itemName = itemView.findViewById(R.id.item_name);
            EditText itemPrice = itemView.findViewById(R.id.item_price);
            ImageView deleteButton = itemView.findViewById(R.id.btn_delete);

            itemName.setText(item.getName());
            itemPrice.setText(String.valueOf(item.getPrice()));

            deleteButton.setOnClickListener(v -> {
                menuItemsContainer.removeView(itemView);
                menuItems.remove(item);
                db.collection("vendors").document(vendorId).update("menuItems", menuItems);
                Toast.makeText(getContext(), "Item deleted", Toast.LENGTH_SHORT).show();
            });

            menuItemsContainer.addView(itemView);
        }
    }

    private void addMenuItem() {
        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.menu_item_view, menuItemsContainer, false);
        ImageView deleteButton = itemView.findViewById(R.id.btn_delete);

        deleteButton.setOnClickListener(v -> menuItemsContainer.removeView(itemView));
        menuItemsContainer.addView(itemView);
    }

    private void saveVendorProfile() {
        String name = stallName.getText().toString();
        String categoryText = category.getText().toString();
        String contact = contactNumber.getText().toString();
        boolean isOnline = switchOnline.isChecked();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(contact)) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        List<MenuItem> updatedMenu = new ArrayList<>();
        for (int i = 0; i < menuItemsContainer.getChildCount(); i++) {
            View itemView = menuItemsContainer.getChildAt(i);
            EditText itemName = itemView.findViewById(R.id.item_name);
            EditText itemPrice = itemView.findViewById(R.id.item_price);

            String menuItemName = itemName.getText().toString();
            String menuItemPriceText = itemPrice.getText().toString();
            if (!TextUtils.isEmpty(menuItemName) && !TextUtils.isEmpty(menuItemPriceText)) {
                double menuItemPrice = Double.parseDouble(menuItemPriceText);
                updatedMenu.add(new MenuItem(menuItemName, menuItemPrice));
            }
        }

        Map<String, Object> vendorData = new HashMap<>();
        vendorData.put("stallName", name);
        vendorData.put("category", categoryText);
        vendorData.put("contactNumber", contact);
        vendorData.put("isOnline", isOnline);
        vendorData.put("menuItems", updatedMenu);

        if (isImageChanged) {
            vendorData.put("profileImageEncrypted", encryptedImageString);
        }

        if (isOnline) {
            fetchLiveLocationAndSave(vendorData);
        } else {
            vendorData.put("location", null);
            vendorData.put("address", null);
            db.collection("vendors").document(vendorId).set(vendorData, SetOptions.merge())
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(getContext(), "Profile saved successfully. Status: OFFLINE", Toast.LENGTH_LONG).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error saving profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        }
    }

    private void fetchLiveLocationAndSave(Map<String, Object> vendorData) {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            Toast.makeText(getContext(), "Location permission required", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    if (!addresses.isEmpty()) {
                        String address = addresses.get(0).getAddressLine(0);
                        vendorData.put("location", new GeoPoint(latitude, longitude));
                        vendorData.put("address", address);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                db.collection("vendors").document(vendorId).set(vendorData, SetOptions.merge())
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(getContext(), "Profile saved successfully. Status: ONLINE", Toast.LENGTH_LONG).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Error saving profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });

            }
        });
    }
}
