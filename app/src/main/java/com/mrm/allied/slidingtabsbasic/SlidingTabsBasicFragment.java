/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mrm.allied.slidingtabsbasic;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.mrm.R;
import com.mrm.db.service.AlliedService;
import com.mrm.db.service.PulseService;
import com.mrm.db.service.UtilityService;
import com.mrm.editPulse.EditPulseActivity;
import com.mrm.editPulse.ViewPulseActivity;
import com.mrm.image.FullScreenImage;
import com.mrm.slidingTabs.common.view.SlidingTabLayout;
import com.mrm.to.Allied;
import com.mrm.to.Pulse;
import com.mrm.to.factoryPackage.PulseFactory;
import com.mrm.util.Constants;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * A basic sample which shows how to use {@link SlidingTabLayout}
 * to display a custom {@link ViewPager} title strip which gives continuous feedback to the user
 * when scrolling.
 */
public class SlidingTabsBasicFragment extends Fragment {

    static final String LOG_TAG = "SlidingTabsBasicFragment";
    int currentPage=0;

    AlliedService alliedService;
    PulseService pulseService;
    // UI references
    //private GetAllPulsesForAlliedTask task;
    SamplePagerAdapter samplePagerAdapter;
    Allied allied;
    UtilityService utilityService;
    GetAllPulsesForAlliedTask task;
    private static SimpleDateFormat formatter;
    /**
     * A custom {@link ViewPager} title strip which looks much like Tabs present in Android v4.0 and
     * above, but is designed to give continuous feedback to the user when scrolling.
     */
    private SlidingTabLayout mSlidingTabLayout;

    /**
     * A {@link ViewPager} which will be used in conjunction with the {@link SlidingTabLayout} above.
     */
    private ViewPager mViewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // alliedDAO = new AlliedDAO(getActivity());
        //pulseDAO = new PulseDAO(getActivity());

        pulseService = new PulseService(getActivity());
        alliedService = new AlliedService(getActivity());
        formatter = new SimpleDateFormat(
                getActivity().getResources().getString(R.string.date_format));

    }


    @Override
    public void onPause() {
        super.onPause();
        pulseService.closeDB();
        alliedService.closeDB();
    }

    /**
     * Inflates the {@link View} which will be displayed by this {@link Fragment}, from the app's
     * resources.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View view =inflater.inflate(R.layout.allied_slidingtab_fragment_sample, container, false);
        View refToAsyncTask = getActivity().getLayoutInflater().inflate(R.layout.allied_slidingtab_pager_item,
                container, false);
        ArrayList list= getActivity().getIntent().getParcelableArrayListExtra("selectedAllied");
        allied =(Allied) list.get(0);

        utilityService = new UtilityService(getActivity());
        GetAllPulsesForAlliedTask task = new GetAllPulsesForAlliedTask(getActivity(),refToAsyncTask);
        task.execute((Void) null);
        //pulses = pulseService.getPulsesWithImagesForAlliedId(allied.getId());

        return view;
    }

    public void updateView(int currentPageNum){
        ArrayList list= getActivity().getIntent().getParcelableArrayListExtra("selectedAllied");
        allied =(Allied) list.get(0);

        /*int totalPulses =0;
        totalPulses = pulseService.countPulsesForAlliedId(allied.getId());

        int totalPages = (totalPulses % PulseFactory.poolSize ==0) ? totalPulses/PulseFactory.poolSize :
                totalPulses/PulseFactory.poolSize + 1;
        if(currentPage >= totalPages-1) {
            currentPage=totalPages-1;
            return;
        }
        else currentPage++;*/
        currentPage = currentPageNum;
        View refToAsyncTask = getActivity().getLayoutInflater().inflate(R.layout.allied_slidingtab_pager_item,
                null, false);
        task = new GetAllPulsesForAlliedTask(getActivity(),refToAsyncTask);
        task.execute((Void) null);
    }






    @Override
    public void onResume() {
        if(utilityService.isRefreshRequired(Constants.REFRESH_DEPENDENT_ID).getVal() != null) {
            utilityService.updateRefreshRequiredValue(Constants.REFRESH_DEPENDENT_ID, null);
            updateView(0);
        }
        //updateView();
        super.onResume();
    }

    // BEGIN_INCLUDE (fragment_onviewcreated)
    /**
     * This is called after the {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} has finished.
     * Here we can pick out the {@link View}s we need to configure from the content view.
     *
     * We set the {@link ViewPager}'s adapter to be an instance of {@link SamplePagerAdapter}. The
     * {@link SlidingTabLayout} is then given the {@link ViewPager} so that it can populate itself.
     *
     * @param view View created in {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // BEGIN_INCLUDE (setup_viewpager)
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        //mViewPager.setAdapter(new SamplePagerAdapter());
        // END_INCLUDE (setup_viewpager)

        // BEGIN_INCLUDE (setup_slidingtablayout)
        // Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
        // it's PagerAdapter set.
        mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);

        // END_INCLUDE (setup_slidingtablayout)
    }
    // END_INCLUDE (fragment_onviewcreated)

    public class GetAllPulsesForAlliedTask extends AsyncTask<Void, Void, ArrayList<Pulse>> {

        private final WeakReference<Activity> activityWeakRef;
        private final View view;

        public GetAllPulsesForAlliedTask(Activity context,View view) {
            this.activityWeakRef = new WeakReference<Activity>(context);
            this.view = view;
        }

        @Override
        protected ArrayList<Pulse> doInBackground(Void... arg0) {
            ArrayList<Pulse> pulseList = pulseService.getPulsesWithImagesForAlliedId(allied.getId(),currentPage);
            return pulseList;
        }

        @Override
        protected void onPostExecute(ArrayList<Pulse> pulseList) {

            if (activityWeakRef.get() != null
                    && !activityWeakRef.get().isFinishing()) {
//                Log.d("allied", alliedList.toString());

                if (pulseList != null) {
                    if (pulseList.size() != 0){
                        samplePagerAdapter = new SamplePagerAdapter(pulseList);
                        mViewPager.setAdapter(samplePagerAdapter);
                    } else {
                        samplePagerAdapter = new SamplePagerAdapter(pulseList);
                        mViewPager.setAdapter(samplePagerAdapter);
                        //Toast.makeText(getActivity(), getString(R.string.itemsNotExist),Toast.LENGTH_LONG).show();
                    }
                    for(Pulse pulse : pulseList){
                        if( pulse.getImage()!= null
                                && pulse.getImage().setContext(this.activityWeakRef.get()).getImageUriFromAnyWhere()!= null) {
                            //pulse.getImage().setContext(this.activityWeakRef.get());
                            pulse.getImage().loadImageForPager(samplePagerAdapter,this.view);
                        }
                    }
                    mSlidingTabLayout.setViewPager(mViewPager);
                }

            }
        }
    }

    /**
     * The {@link PagerAdapter} used to display pages in this sample.
     * The individual pages are simple and just display two lines of text. The important section of
     * this class is the {@link #getPageTitle(int)} method which controls what is displayed in the
     * {@link SlidingTabLayout}.
     */
    class SamplePagerAdapter extends PagerAdapter {

        /**
         * @return the number of pages to display
         */
        ArrayList<Pulse> pulseList;

        @Override
        public int getCount() {
            if(this.pulseList.size()==0)
                return 1;
            return this.pulseList.size();
        }

        public SamplePagerAdapter(ArrayList<Pulse> pulseList){
            this.pulseList = pulseList;
        }

        /**
         * @return true if the value returned from {@link #instantiateItem(ViewGroup, int)} is the
         * same object as the {@link View} added to the {@link ViewPager}.
         */
        @Override
        public boolean isViewFromObject(View view, Object o) {
            return o == view;
        }

        // BEGIN_INCLUDE (pageradapter_getpagetitle)
        /**
         * Return the title of the item at {@code position}. This is important as what this method
         * returns is what is displayed in the {@link SlidingTabLayout}.
         * <p>
         * Here we construct one using the position value, but for real application the title should
         * refer to the item's contents.
         */
        @Override
        public CharSequence getPageTitle(int position) {
            if(this.pulseList.size() ==0)
                return getString(R.string.itemsNotExist);
            return (this.pulseList.get(position).getName().equals("")  ?
                ((currentPage*PulseFactory.poolSize)+position+1)+ " : " + getString(R.string.pulse_name_hint) :
                ((currentPage*PulseFactory.poolSize)+position+1)+ " : " + this.pulseList.get(position).getName()
            );
        }
        // END_INCLUDE (pageradapter_getpagetitle)

        /**
         * Instantiate the {@link View} which should be displayed at {@code position}. Here we
         * inflate a layout from the apps resources and then change the text view to signify the position.
         */
        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            // Inflate a new layout from our resources
            View view = getActivity().getLayoutInflater().inflate(R.layout.allied_slidingtab_pager_item,
                    container, false);
            // Add the newly created View to the ViewPager
            container.addView(view);
            if(this.pulseList.size() ==0) {
                TextView title = (TextView) view.findViewById(R.id.etxt_name);
                title.setText(getString(R.string.itemsNotExist));
                ((Button)view.findViewById(R.id.btn_edit_pulse)).setVisibility(View.GONE);
                ((Button)view.findViewById(R.id.btn_view_pulse_detail)).setVisibility(View.GONE);

                return view;
            }
            // Retrieve a TextView from the inflated View, and update it's text
            TextView updatedDate = (TextView) view.findViewById(R.id.updatedDate);
            updatedDate.setText(formatter.format(this.pulseList.get(position).getDateOfBirth()));

            TextView name = (TextView) view.findViewById(R.id.etxt_name);
            name.setText(this.pulseList.get(position).getName());

            TextView desc = (TextView) view.findViewById(R.id.etxt_pulse_desc);
            desc.setText(this.pulseList.get(position).getPulseDesc());

            TextView id = (TextView) view.findViewById(R.id.pulseContentId);
            id.setText(this.pulseList.get(position).getId()+"");

            ImageView img = (ImageView) view.findViewById(R.id.pulseImg);
            img.setTag(this.pulseList.get(position).getImage().setContext(getActivity()).getImageUriFromAnyWhere());
            BitmapFactory.Options options = new BitmapFactory.Options();
            //Convert bytearray to bitmap
            if(this.pulseList.get(position).getImage()!= null && this.pulseList.get(position).getImage().getBitmap()!= null) {
                img.setImageBitmap(this.pulseList.get(position).getImage().getBitmap());
                img.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        Intent fullScreenIntent = new Intent(getActivity(), FullScreenImage.class);
                        fullScreenIntent.putExtra("imageUriPath", (String) view.getTag());
                        getActivity().startActivity(fullScreenIntent);
                        utilityService.updateRefreshRequiredValue(Constants.REFRESH_DEPENDENT_ID, null);
                        //Toast.makeText(mContext, "ImageView clicked for the row = " + view.getTag().toString(), Toast.LENGTH_SHORT).show();
                    }
                });

                /*img.setImageBitmap(
                        BitmapFactory.decodeByteArray(pulses.get(position).getImage().getImage()
                                , 0, pulses.get(position).getImage().getImage().length, options));*/
            }else
            {
                img.setImageBitmap(null);
                img.setTag(null);
                img.setOnClickListener(null);
            }

            Button btn_view_pulse_detail = (Button) view.findViewById(R.id.btn_view_pulse_detail);
            btn_view_pulse_detail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Do things Here
                    //System.out.println("btn_view_pulse_detail.onClick: "+ pulseList.get(position) );
                    Intent intent = new Intent(getActivity(),
                            ViewPulseActivity.class);
                    ArrayList<Pulse> pulseList1 = new ArrayList();
                    pulseList1.add(pulseList.get(position));
                    intent.putParcelableArrayListExtra("selectedPulse", pulseList1);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    ////System.out.println("selectedPulse.getName: " + selectedPulse.getName());
                    startActivity(intent);
                }
            });

            Button btn_edit_pulse = (Button) view.findViewById(R.id.btn_edit_pulse);
            btn_edit_pulse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Do things Here
                    //System.out.println("btn_edit_pulse.onClick: "+ pulseList.get(position) );
                    Intent intent = new Intent(getActivity(),
                            EditPulseActivity.class);
                    ArrayList<Pulse> pulseList1 = new ArrayList();
                    pulseList1.add(pulseList.get(position));
                    intent.putParcelableArrayListExtra("selectedPulse", pulseList1);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    ////System.out.println("selectedPulse.getName: " + selectedPulse.getName());
                    startActivity(intent);
                }
            });

            ((RelativeLayout) view.findViewById(R.id.layout_sliding_item))
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Do things Here
                            //System.out.println("btn_edit_pulse.onClick: " + pulseList.get(position));
                            showViewPulseScreen(position);
                        }
                    });



            //title.setText(pulses.get(position).getName());

            //Log.i(LOG_TAG, "instantiateItem() [position: " + position + "]");

            // Return the View
            return view;
        }

        /**
         * Destroy the item from the {@link ViewPager}. In our case this is simply removing the
         * {@link View}.
         */
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
            //Log.i(LOG_TAG, "destroyItem() [position: " + position + "]");
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;

        }

        public void showViewPulseScreen(int position)
        {
            Pulse selectedPulse = pulseList.get(position);
            //System.out.println("btn_view_pulse_detail.onClick: " + selectedPulse);
            Intent intent = new Intent(getActivity(),
                    ViewPulseActivity.class);
            ArrayList<Pulse> pulseList = new ArrayList();
            pulseList.add(selectedPulse);
            intent.putParcelableArrayListExtra("selectedPulse", pulseList);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            ////System.out.println("selectedPulse.getName: " + selectedPulse.getName());
            startActivity(intent);
        }

    }


}
