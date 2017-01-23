/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dheeraj.com.trafficsolution;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import dheeraj.com.trafficsolution.Activities.LoginRegisterChoose;
import dheeraj.com.trafficsolution.Activities.TaxiMeter;
import dheeraj.com.trafficsolution.Utils.SharedPreferenceMethods;

public class FeedsActivity extends AppCompatActivity implements PostsFragment.OnPostSelectedListener {
    private static final String TAG = "FeedsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (SharedPreferenceMethods.getString(FeedsActivity.this, SharedPreferenceMethods.IS_LOGGED_IN).equals("yes")) {
            init();
        }
        else {
            startActivity(new Intent(FeedsActivity.this, LoginRegisterChoose.class));
            finish();
        }
    }
    
    void init() {
        setContentView(R.layout.activity_feeds);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toast.makeText(FeedsActivity.this, "Please Wait. Loading Feed...", Toast.LENGTH_LONG).show();
        Toast.makeText(FeedsActivity.this, "Please Wait. Loading Feed...", Toast.LENGTH_LONG).show();

        ViewPager viewPager = (ViewPager) findViewById(R.id.feeds_view_pager);
        FeedsPagerAdapter adapter = new FeedsPagerAdapter(getSupportFragmentManager());
        
        adapter.addFragment(new GeoFenceFragment(), "Pedestrian");
        adapter.addFragment(PostsFragment.newInstance(PostsFragment.TYPE_FEED), "Feed");
        adapter.addFragment(new ParkingFragment(), "Parking");
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(1);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.feeds_tab_layout);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onPostComment(String postKey) {
        Intent intent = new Intent(this, CommentsActivity.class);
        intent.putExtra(CommentsActivity.POST_KEY_EXTRA, postKey);
        startActivity(intent);
    }

    @Override
    public void onPostLike(final String postKey) {
        final String userKey = FirebaseUtil.getCurrentUserId();
        final DatabaseReference postLikesRef = FirebaseUtil.getLikesRef();
        postLikesRef.child(postKey).child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // User already liked this post, so we toggle like off.
                    postLikesRef.child(postKey).child(userKey).removeValue();
                } else {
                    postLikesRef.child(postKey).child(userKey).setValue(ServerValue.TIMESTAMP);
                }
            }
            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_feeds, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            // TODO: Add settings screen.
            return true;
        }
        if (id == R.id.action_logout) {
            SharedPreferenceMethods.setString(FeedsActivity.this, SharedPreferenceMethods.IS_LOGGED_IN, "no");
            startActivity(new Intent(FeedsActivity.this, LoginRegisterChoose.class));
            finish();
            return true;
        }
        else if (id == R.id.action_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
            return true;
        }
        else if (id == R.id.action_taxiMeter) {
            startActivity(new Intent(this, TaxiMeter.class));
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class FeedsPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public FeedsPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5 && keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    public void onBackPressed() {
        Toast.makeText(FeedsActivity.this, "exit", Toast.LENGTH_SHORT).show();
        finish();
    }
}
