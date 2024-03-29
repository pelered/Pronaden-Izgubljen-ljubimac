package com.example.pomozi;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pomozi.Adapter.IspisAdapterZiv;
import com.example.pomozi.Adapter.ProfileMyAdapter;
import com.example.pomozi.Helper.CustomSpinner;
import com.example.pomozi.Model.ZivUpload;
import com.example.pomozi.R;
import com.facebook.login.LoginFragment;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerViewHome;
    private IspisAdapterZiv myAdapter;
    private LinearLayoutManager layoutManager;
    private Button dodaj_obajvu;
    private CustomSpinner spinner;
    private String grad,zup;
    private ZivUpload ziv= new ZivUpload();
    private List<ZivUpload> itemList =new ArrayList<>();
    private String sort;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        if (container != null) {
            container.removeAllViews();
        }
        return inflater.inflate(R.layout.fragment_home, container, false);
    }
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        recyclerViewHome =view.findViewById(R.id.recycler_home);
        recyclerViewHome.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        layoutManager.scrollToPosition(0);
        recyclerViewHome.setLayoutManager(layoutManager);
        recyclerViewHome.scrollToPosition(0);
        SharedPreferences prefs = requireContext().getSharedPreferences("shared_pref_name", Context.MODE_PRIVATE);
        grad=prefs.getString("grad","");
        zup=prefs.getString("zupanija","");
        //Log.d("HomeF",grad+zup);
        //spiner
        String[] data = {"Najnovije", "Grad", "Županija"};

        ArrayAdapter adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item_selected, data);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        sort=prefs.getString("sort","");

        spinner = (CustomSpinner) view.findViewById(R.id.spinner);
        spinner.setAdapter(adapter);
        if(!sort.equals("")){
            spinner.setSelection(Arrays.asList(data).indexOf(sort));
        }
        spinner.setSpinnerEventsListener(new CustomSpinner.OnSpinnerEventsListener() {
            public void onSpinnerOpened() {
                spinner.setSelected(true);

            }
            public void onSpinnerClosed() {
                spinner.setSelected(false);
                SharedPreferences prefs = requireContext().getSharedPreferences("shared_pref_name", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("sort",spinner.getSelectedItem().toString());
                editor.apply();
                generateItem();

            }
        });

        if(!prefs.getString("uid","").equals("")){
            dodaj_obajvu=view.findViewById(R.id.dodaj_objavu);
            dodaj_obajvu.setVisibility(View.VISIBLE);
            dodaj_obajvu.setOnClickListener(v -> {
                FragmentTransaction ft = requireActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.nav_host_fragment,new EditZiv());
                ft.addToBackStack("home_fragment");
                ft.commit();
            });
        }else{
            dodaj_obajvu=view.findViewById(R.id.dodaj_objavu);
            dodaj_obajvu.setVisibility(View.GONE);
            spinner.setVisibility(View.GONE);
        }

        //Log.d("Home",grad);
        generateItem();
    }

    private void generateItem() {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Ziv");
        Query query = ref.orderByChild("last_date").limitToFirst(100);

        //Query query = ref.orderByChild("last_date").limitToFirst(10);
        //
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                itemList.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    //Log.d("Ispisujem",postSnapshot.child("grad").getValue().toString());
                    if(spinner.getSelectedItem().toString().equals("Grad") && !grad.equals("")){
                        if(postSnapshot.child("grad").getValue().toString().contains(grad) || grad.contains(postSnapshot.child("grad").getValue().toString())){
                            Log.d("Ispisujem",postSnapshot.getValue().toString());
                            ziv = postSnapshot.getValue(ZivUpload.class);
                            if (ziv != null) {
                                ziv.setKey(postSnapshot.getKey());
                                //Log.d("generateItem", ziv.toString());
                                itemList.add(ziv);
                            }
                        }
                    }else if(spinner.getSelectedItem().toString().equals("Županija") && !zup.equals("")){
                        if(postSnapshot.child("zupanija").getValue().toString().contains(zup) || zup.contains(postSnapshot.child("zupanija").getValue().toString())){
                            Log.d("Ispisujem",postSnapshot.getValue().toString());
                            ziv = postSnapshot.getValue(ZivUpload.class);
                            if (ziv != null) {
                                ziv.setKey(postSnapshot.getKey());
                                //Log.d("generateItem", ziv.toString());
                                itemList.add(ziv);
                            }
                        }
                    }else{
                        Log.d("Ispisujem",postSnapshot.getValue().toString());
                        ziv = postSnapshot.getValue(ZivUpload.class);
                        if (ziv != null) {
                            ziv.setKey(postSnapshot.getKey());
                            //Log.d("generateItem", ziv.toString());
                            itemList.add(ziv);
                        }


                    }

                }
                layoutManager.setReverseLayout(true);
                layoutManager.scrollToPosition(0);
                layoutManager.setStackFromEnd(true);
                recyclerViewHome.scrollToPosition(0);
                myAdapter = new IspisAdapterZiv(getContext(), itemList);
                recyclerViewHome.setAdapter(myAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("onCancelled:fav: ",databaseError.getMessage());
            }
        });

    }
    public void onStart() {
        super.onStart();



    }
}
