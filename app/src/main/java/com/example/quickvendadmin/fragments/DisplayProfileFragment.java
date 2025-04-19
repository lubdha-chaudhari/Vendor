package com.example.quickvendadmin.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.quickvendadmin.MenuItem;
import com.example.quickvendadmin.R;
import com.example.quickvendadmin.Vendor;
import com.example.quickvendadmin.VendorLoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class DisplayProfileFragment extends Fragment {
    private ImageButton btnLogout;

    private TextView tvStallName, tvCategory, tvContact, tvAddress, tvOnlineStatus;
    private ImageView ivProfileImage;
    private LinearLayout layoutMenuItems;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String vendorId;

    public DisplayProfileFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_display_profile, container, false);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        vendorId = auth.getCurrentUser().getUid();

        tvStallName = view.findViewById(R.id.tv_stall_name);
        tvCategory = view.findViewById(R.id.tv_category);
        tvContact = view.findViewById(R.id.tv_contact);
        tvAddress = view.findViewById(R.id.tv_address);
        tvOnlineStatus = view.findViewById(R.id.tv_online_status);
        ivProfileImage = view.findViewById(R.id.iv_profile_image);
        layoutMenuItems = view.findViewById(R.id.layout_menu_items);

        // Find the logout button and set onClickListener
        btnLogout = view.findViewById(R.id.btn_logout);  // Reference the logout button
        btnLogout.setOnClickListener(v -> logoutUser());

        fetchVendorData();

        return view;
    }

    private void fetchVendorData()  {
        db.collection("vendors").document(vendorId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Vendor vendor = documentSnapshot.toObject(Vendor.class);
                        if (vendor != null) {
                            tvStallName.setText(vendor.getStallName());
                            tvCategory.setText(vendor.getCategory());
                            tvContact.setText(vendor.getContactNumber());
                            tvAddress.setText(vendor.getAddress());
                            tvOnlineStatus.setText(vendor.isOnline() ? "Active" : "Not Active");

                            // Load and transform profile image into a circular shape using Glide
                            String base64Image = vendor.getProfileImageEncrypted();
                            if (base64Image != null && !base64Image.isEmpty()) {
                                byte[] decodedBytes = Base64.decode(base64Image, Base64.NO_WRAP);
                                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

                                // Using Glide to apply circular transformation and fit the image properly
                                Glide.with(getContext())
                                        .load(bitmap)
                                        .circleCrop() // Apply circular crop
                                        .into(ivProfileImage); // Set the transformed image to ImageView
                            }

                            // Load menu items
                            layoutMenuItems.removeAllViews();
                            List<MenuItem> menu = vendor.getMenuItems();
                            if (menu != null) {
                                for (MenuItem item : menu) {
                                    TextView itemView = new TextView(getContext());
                                    itemView.setText("- " + item.getName() + ": ₹" + item.getPrice());
                                    itemView.setTextSize(16);
                                    layoutMenuItems.addView(itemView);
                                }
                            }
                        }
                    } else {
                        Toast.makeText(getContext(), "Vendor profile not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void logoutUser() {
        // Log out the user from Firebase Authentication
        FirebaseAuth.getInstance().signOut();

        // Show a Toast message for logout
        Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Redirect to LoginActivity
        Intent intent = new Intent(getActivity(), VendorLoginActivity.class);
        startActivity(intent);

        // Finish the current activity/fragment to prevent back navigation
        getActivity().finish();
    }

}
