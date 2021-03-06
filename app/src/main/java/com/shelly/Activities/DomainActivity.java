package com.shelly.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.shelly.R;
import com.shelly.Adapters.DomainListAdapter;

import java.util.ArrayList;
import java.util.List;

public class DomainActivity extends AppCompatActivity {

    //Views
    private Button mFinishSetUpBtn;
    private RecyclerView mDomainListRV;
    private DomainListAdapter mDomainListAdapter;

    //Firebase
    private FirebaseUser mUser;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRefDatabase;

    //Variables
    private List<String> mDomainList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_domain);

        //Firebase Binding
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();
        mRefDatabase = mDatabase.getReference();

        //Variables Binding
        mDomainList = new ArrayList<>();
        initializeData();

        //View Binding
        mFinishSetUpBtn = findViewById(R.id.FinishSetUpButton);

        //Implementing Functionalities
        mFinishSetUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRefDatabase.child(getString(R.string.dbfield_ambassadors)).
                                child(mUser.getUid()).
                                    setValue(mDomainListAdapter.getSelectedDomains());
                Intent i = new Intent(DomainActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    public void initializeData() {
        Query query = mRefDatabase.child(getString(R.string.dbfield_resources)).
                child(getString(R.string.dbfiled_domains_resources)).
                child(getString(R.string.dbfield_domain_list));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    Log.e("Datasnaphot", ""+ds);
                    String mDomain = (String) ds.getValue();
                    mDomainList.add(mDomain);
                }
                mDomainListRV = findViewById(R.id.DomainsListRecyclerView);
                mDomainListAdapter = new DomainListAdapter(mDomainList, DomainActivity.this);
                mDomainListRV.setLayoutManager(new LinearLayoutManager(DomainActivity.this));
                mDomainListRV.setAdapter(mDomainListAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
