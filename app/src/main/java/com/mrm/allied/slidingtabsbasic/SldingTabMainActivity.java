/*
* Copyright 2013 The Android Open Source Project
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


package com.mrm.allied.slidingtabsbasic;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.mrm.R;
import com.mrm.allied.editAllied.EditAlliedActivity;
import com.mrm.allied.editAllied.ViewAlliedActivity;
import com.mrm.db.AlliedDAO;
import com.mrm.db.service.AlliedService;
import com.mrm.db.service.PulseService;
import com.mrm.db.service.UtilityService;
import com.mrm.image.FullScreenImage;
import com.mrm.preference.AboutDependentScreen;
import com.mrm.slidingTabs.common.activities.SlidingTabActivityBase;
import com.mrm.slidingTabs.common.logger.Log;
import com.mrm.slidingTabs.common.logger.LogWrapper;
import com.mrm.slidingTabs.common.logger.MessageOnlyLogFilter;
import com.mrm.to.Allied;
import com.mrm.to.factoryPackage.PulseFactory;
import com.mrm.util.Constants;
import com.mrm.util.ImageRenderFactory;

import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * A simple launcher activity containing a summary sample description, sample log and a custom
 * {@link android.support.v4.app.Fragment} which can display a view.
 * <p>
 * For devices with displays with a width of 720dp or greater, the sample log is always visible,
 * on other devices it's visibility is controlled by an item on the Action Bar.
 */
public class SldingTabMainActivity extends SlidingTabActivityBase {

    public static final String TAG = "SldingTabMainActivity";

    // Whether the Log Fragment is currently shown
    private boolean mLogShown;
    AlliedDAO alliedDAO;
    AlliedService alliedService;
    UtilityService utilityService;
    int currentPage=0;
    private TextView alliedNameText;
    private TextView alliedDescText;
    private TextView alliedContentIdEtxt;
    private TextView alliedImageIdEtxt;
    private ImageView alliedImageContentView;
    PulseService pulseService;
    TextView pageNum;
    LinearLayout layout_RootForSliding;
    Button btn_next_page;
    Button btn_prev_page;

    @Override
    protected void onPause() {
        super.onPause();
        alliedService.closeDB();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.allied_slidingtab_activity_main);
        alliedDAO = new AlliedDAO(this);
        alliedService = new AlliedService(this);
        pulseService = new PulseService(this);
        findSlidingAlliedViewsById();
        ArrayList list= getIntent().getParcelableArrayListExtra("selectedAllied");
        Allied allied =(Allied) list.get(0);
        //System.out.println("SldingTabMainActivity.onCreate: allied id : " + allied.getId());
        allied = alliedService.getAlliedWithImage(allied.getId());
        setAlliedFromDB(allied);

        utilityService = new UtilityService(this);

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            SlidingTabsBasicFragment fragment = new SlidingTabsBasicFragment();
            transaction.replace(R.id.sample_content_fragment, fragment);
            transaction.commit();
        }


        int totalPulses = pulseService.countPulsesForAlliedId(allied.getId());
        if(totalPulses > PulseFactory.poolSize) {
            int totalPages = (totalPulses % PulseFactory.poolSize == 0) ? totalPulses / PulseFactory.poolSize :
                    totalPulses / PulseFactory.poolSize + 1;
			/*if ((currentPage+1) >= totalPages) {
				currentPage = currentPage;
			}*/
            pageNum.setText(getString(R.string.page)+" "+ (currentPage+1) +" : " + getString(R.string.of)+" "+ totalPages);
        }else pageNum.setText(getString(R.string.page)+" "+ (currentPage+1) +" : " + getString(R.string.of)+" "+ 1);


        btn_next_page.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                nextPage();
            }
        });

        btn_prev_page.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                previousPage();
            }
        });

        layout_RootForSliding.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showViewAlliedScreen();
            }
        });

    }

    private void findSlidingAlliedViewsById() {
        alliedNameText = (TextView) findViewById(R.id.etxt_name);
        alliedDescText = (TextView) findViewById(R.id.etxt_allied_desc);
        alliedContentIdEtxt= (TextView) findViewById(R.id.alliedContentId);
        alliedImageIdEtxt = (TextView) findViewById(R.id.alliedImageContentId);
        alliedImageContentView = (ImageView) findViewById(R.id.alliedImg);
        layout_RootForSliding = (LinearLayout) findViewById(R.id.layout_RootForSliding);

        btn_next_page = (Button) findViewById(R.id.btn_next_page);
        btn_prev_page = (Button) findViewById(R.id.btn_prev_page);
        pageNum = (TextView) findViewById(R.id.pageNum);
    }

    private void setAlliedFromDB(Allied allied) {
        alliedNameText.setText(allied.getName());
        alliedDescText.setText(allied.getAlliedDesc());

        alliedContentIdEtxt.setText(String.valueOf(allied.getId()));
        alliedImageIdEtxt.setText(String.valueOf(allied.getAlliedImageId()));
        String uriString = allied.getAlliedImage().setContext(this).getImageUriFromAnyWhere();

        if(allied.getAlliedImage() != null && allied.getAlliedImage().getID() != -1 && uriString!= null) {
            Uri capturedImageUri = Uri.parse(uriString);
            try {
                ImageRenderFactory imageRenderFactory = new ImageRenderFactory();
                alliedImageContentView.setImageBitmap(imageRenderFactory.decodeSampledBitmapFromResourceMemOpt(
                        getContentResolver().openInputStream(capturedImageUri),
                        Constants.WIDTH, Constants.HEIGHT));
                alliedImageContentView.setTag(uriString);
                alliedImageContentView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        Intent fullScreenIntent = new Intent(view.getContext(), FullScreenImage.class);
                        fullScreenIntent.putExtra("imageUriPath", (String) view.getTag());
                        view.getContext().startActivity(fullScreenIntent);
                        utilityService.updateRefreshRequiredValue(Constants.REFRESH_DEPENDENT_ID, null);
                        //Toast.makeText(mContext, "ImageView clicked for the row = " + view.getTag().toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }



        //pulseCreatedDateEtxt.setText(formatter.format(pulse.getDateOfBirth()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.slidingtab_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        /*MenuItem logToggle = menu.findItem(R.id.menu_toggle_log);
        logToggle.setVisible(findViewById(R.id.sample_output) instanceof ViewAnimator);
        logToggle.setTitle(mLogShown ? R.string.sample_hide_log : R.string.sample_show_log);
*/
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.editRoot:

                showEditAlliedScreen(Integer.parseInt(alliedContentIdEtxt.getText().toString()));
                return true;

            case R.id.addDependent:
                Intent intent = new Intent("add_pulse_activity");
                /*BN.putString("user_name",NAME );
                BN.putString("user_pass",userpass );
                i.putExtras(BN);*/
                Allied selectedAllied = new Allied();
                selectedAllied.setId(Integer.parseInt(alliedContentIdEtxt.getText().toString()));
                ArrayList<Allied> alliedList = new ArrayList();
                alliedList.add(selectedAllied);
                intent.putParcelableArrayListExtra("selectedAlliedForAddDependent", alliedList);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;

            case android.R.id.home:
                finish();
                return true;

            case R.id.aboutDependentScreen:
                Intent i = new Intent(this, AboutDependentScreen.class);
                startActivity(i);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showEditAlliedScreen(int alliedId) {
        /*alliedDAO = new AlliedDAO(context);
		long alliedId = alliedDAO.getAlliedIdHavingPulseId(allied.getId());
		*/
        Allied selectedAllied = new Allied();
        selectedAllied.setId(alliedId);

        Intent intent = new Intent(this, EditAlliedActivity.class);
        ArrayList<Allied> alliedList = new ArrayList();
        alliedList.add(selectedAllied);
        intent.putParcelableArrayListExtra("selectedAllied", alliedList);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //System.out.println("selectedAllied.getName: " + selectedAllied.getName());
        startActivity(intent);
    }

    private void showViewAlliedScreen() {
       		/*alliedDAO = new AlliedDAO(context);
				long alliedId = alliedDAO.getAlliedIdHavingPulseId(allied.getId());
*/      int alliedId = Integer.parseInt(alliedContentIdEtxt.getText().toString());

        Allied selectedAllied = new Allied();
        selectedAllied.setId(alliedId);

        Intent intent = new Intent(this,ViewAlliedActivity.class);
        ArrayList<Allied> alliedList = new ArrayList();
        alliedList.add(selectedAllied);
        intent.putParcelableArrayListExtra("selectedAllied", alliedList);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //System.out.println("selectedAllied.getName: " + selectedAllied.getName());
        this.startActivity(intent);
    }

    public void setValuesFromView(){

    }

    /** Create a chain of targets that will receive log data */
    @Deprecated
    @Override
    public void initializeLogging() {
        // Wraps Android's native log framework.
        LogWrapper logWrapper = new LogWrapper();
        // Using Log, front-end to the logging chain, emulates android.util.log method signatures.
        Log.setLogNode(logWrapper);

        // Filter strips out everything except the message text.
        MessageOnlyLogFilter msgFilter = new MessageOnlyLogFilter();
        logWrapper.setNext(msgFilter);

        // On screen logging via a fragment with a TextView.
        /*LogFragment logFragment = (LogFragment) getSupportFragmentManager()
                .findFragmentById(R.id.log_fragment);
        msgFilter.setNext(logFragment.getLogView());*/

        Log.i(TAG, "Ready1");
    }

    @Override
    public void onResume() {
        this.setTitle(R.string.sliding_items_allied);
       ArrayList list= getIntent().getParcelableArrayListExtra("selectedAllied");
        Allied allied =(Allied) list.get(0);
        //currentPage=0;
        //System.out.println("SldingTabMainActivity.onResume: allied id : "+allied.getId());
        allied = alliedService.getAlliedWithImage(allied.getId());
        setAlliedFromDB(allied);


        /*FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        SlidingTabsBasicFragment fragment = new SlidingTabsBasicFragment();
        transaction.replace(R.id.sample_content_fragment, fragment);
        fragment.updateView();
        transaction.commit();*/
        super.onResume();

    }


    public void updateView(){
        //System.out.println("SldingTabMainActivity.updateView");

        int totalPulses =0;
        totalPulses = pulseService.countPulsesForAlliedId(Integer.parseInt(alliedContentIdEtxt.getText().toString()));

        int totalPages = (totalPulses % PulseFactory.poolSize ==0) ? totalPulses/PulseFactory.poolSize :
                totalPulses/PulseFactory.poolSize + 1;
        if(currentPage >= totalPages-1) {
            currentPage=totalPages-1;
            return;
        }
        else currentPage++;

        pageNum.setText(getString(R.string.page)+" "+(currentPage+1)+" : " + getString(R.string.of)+" "+ totalPages);
    }


    public void nextPage() {
        int totalPulses =0;
        totalPulses = pulseService.countPulsesForAlliedId(Integer.parseInt(alliedContentIdEtxt.getText().toString()));

        int totalPages = (totalPulses % PulseFactory.poolSize ==0) ? totalPulses/PulseFactory.poolSize :
                totalPulses/PulseFactory.poolSize + 1;
        if(currentPage >= totalPages-1) {
            currentPage=totalPages-1;
            return;
        }
        else currentPage++;

        pageNum.setText(getString(R.string.page) + " " + (currentPage + 1) + " : " + getString(R.string.of) + " " + totalPages);
        callRefreshFragment();
        //System.out.println("SldingTabMainActivity.nextPage");
    }

    public void previousPage() {
        int totalPulses =0;
        totalPulses = pulseService.countPulsesForAlliedId(Integer.parseInt(alliedContentIdEtxt.getText().toString()));

        int totalPages = (totalPulses % PulseFactory.poolSize ==0) ? totalPulses/PulseFactory.poolSize :
                totalPulses/PulseFactory.poolSize + 1;
        if(currentPage <= 0) {
            currentPage = 0;
            return;
        }else currentPage--;

        pageNum.setText(getString(R.string.page) + " " + (currentPage + 1) + " : " + getString(R.string.of) + " " + totalPages);

        callRefreshFragment();
        //System.out.println("SldingTabMainActivity.previousPage");

    }

    public void callRefreshFragment(){
        SlidingTabsBasicFragment fragment = (SlidingTabsBasicFragment) getSupportFragmentManager()
                .findFragmentById(R.id.sample_content_fragment);
        if (fragment != null) {
            fragment.updateView(currentPage);
            //fragment.getData();
        }
    }

}
