package com.mrm.db.service;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;


import com.mrm.R;
import com.mrm.db.AlliedDAO;
import com.mrm.db.DataBaseHelper;
import com.mrm.db.UtilityDAO;
import com.mrm.db.backup.GenerateCsv;
import com.mrm.to.Allied;
import com.mrm.to.Pulse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

/**
 * Created by sunjiv6 on 4/1/16.
 */
public class AlliedService {

    UtilityDAO utilityDAO;
    public AlliedDAO alliedDAO;
    private Context context;


    public AlliedService(Context context){
        this.context= context;
        alliedDAO=new AlliedDAO(context);
        alliedDAO.openDB();
    }

    public long save(Allied allied) {
        /*utilityDAO = new UtilityDAO(context);
        utilityDAO.updateRefreshRequiredValue(Constants.REFRESH_ASSOCIATE_ID, Constants.REFRESH_VAL);
        utilityDAO.closeDB();*/
        //alliedDAO.openDB();
        long result = alliedDAO.save(allied);
        //alliedDAO.closeDB();
        return result;
    }

    public long createAlliedFromPulse(Pulse pulse) {
        //alliedDAO.openDB();
        long result = alliedDAO.createAlliedFromPulse(pulse);
        //alliedDAO.closeDB();
        return result;
    }

    public long update(Allied allied) {
        /*utilityDAO = new UtilityDAO(context);
        utilityDAO.updateRefreshRequiredValue(Constants.REFRESH_ASSOCIATE_ID, Constants.REFRESH_VAL);
        utilityDAO.closeDB();*/
        //alliedDAO.openDB();
        long result = alliedDAO.update(allied);
        //alliedDAO.closeDB();
        return result;

    }

    public long updateAsPerOriginalParent(Allied alliedSource) {
        /*utilityDAO = new UtilityDAO(context);
        utilityDAO.updateRefreshRequiredValue(Constants.REFRESH_ASSOCIATE_ID, Constants.REFRESH_VAL);
        utilityDAO.closeDB();*/
        //alliedDAO.openDB();
        long result =-1;
        if(alliedSource.getId() > 0){
            Allied alliedTarget = alliedDAO.getAlliedWithImage(alliedSource.getId());
            alliedTarget.setName(alliedSource.getName());
            alliedTarget.setAlliedDesc(alliedSource.getAlliedDesc());
            alliedTarget.setAlliedImageId(alliedSource.getAlliedImageId());
            result = alliedDAO.update(alliedTarget);
        }
        //alliedDAO.closeDB();
        return result;

    }

    public void updateAlliedBeforeDeletingOriginalParent(long sourcAlliedId) {
        long result =-1;
        if(sourcAlliedId > 0){
            Allied alliedTarget = alliedDAO.getAlliedWithImage(sourcAlliedId);
            alliedTarget.setOriginalPulseId(-1);
            result = alliedDAO.update(alliedTarget);
        }
    }

    public int delete(Allied allied) {
        /*utilityDAO = new UtilityDAO(context);
        utilityDAO.updateRefreshRequiredValue(Constants.REFRESH_ASSOCIATE_ID, Constants.REFRESH_VAL);
        utilityDAO.closeDB();*/
        //alliedDAO.openDB();
        int result = alliedDAO.delete(allied);
        //alliedDAO.closeDB();
        return result;
    }
/*

	public int deleteWithId(String alliedId) {
		return database.delete(DataBaseHelper.ALLIED_TABLE, WHERE_ID_EQUALS,
				new String[] { alliedId});
	}
*/

    //USING query() method
    @Deprecated
    public ArrayList<Allied> getAllieds() {
        ArrayList<Allied> allieds = new ArrayList<Allied>();

        return allieds;
    }

    public ArrayList<Allied> getAlliedsWithImages() {
        ArrayList<Allied> allieds = new ArrayList();
        //alliedDAO.openDB();
        allieds=alliedDAO.getAlliedsWithImages();
        //alliedDAO.closeDB();
        return allieds;
    }

    public ArrayList<Allied> getAlliedsInPages(int pageNum) {
        ArrayList<Allied> allieds = new ArrayList();
        //alliedDAO.openDB();
        allieds=alliedDAO.getAlliedsInPages(pageNum);
        //alliedDAO.closeDB();
        return allieds;
    }

    public ArrayList<Allied> getAlliedsInPages(int pageNum,String alliedName) {
        ArrayList<Allied> allieds = new ArrayList();
        //alliedDAO.openDB();
        allieds=alliedDAO.getAlliedsInPages(pageNum, alliedName);
        //alliedDAO.closeDB();
        return allieds;
    }



    public Allied getAlliedWithImage(long id) {
        Allied allied ;
        //alliedDAO.openDB();
        allied=alliedDAO.getAlliedWithImage(id);
        //alliedDAO.closeDB();
        return allied;
    }


    /**
     * Get Allied/Associate Id of that pulse which is also playing
     * the role of allied/associate
     *
     * */
    public long getAlliedIdHavingPulseId(long pulseId) {
        long ret=0;
        //alliedDAO.openDB();
        ret=alliedDAO.getAlliedIdHavingPulseId(pulseId);
        //alliedDAO.closeDB();
        return ret;
    }

    public int countTotalAllied() {
        return alliedDAO.countTotalAllied();
    }

    public int countSearchedAllied(String searchStr) {
        return alliedDAO.countSearchedAllied(searchStr);
    }

    public void closeDB()
    {
        //alliedDAO.closeDB();
    }


    public void backup() throws Exception{

        Cursor cursor = alliedDAO.backupAllied();
        new GenerateCsv().exportDataInCSV(context,cursor, DataBaseHelper.ALLIED_TABLE);
    }

    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);

    public void restoreEnglish() {

        BufferedReader br = null;

        try {

            String sCurrentLine;

            File folder = Environment.getExternalStoragePublicDirectory(
                    context.getString(R.string.externalDirectoryPath_appFolderName) +
                            File.separator +
                            context.getString(R.string.externalDirectoryPath_Backup)+
                            File.separator +
                            context.getString(R.string.externalDirectoryPath_Database));

            //context.getString(R.string.externalDirectoryDatabasePath));
            //storageDir.mkdirs();
            String filePath = folder.toString() + File.separator + DataBaseHelper.ALLIED_TABLE+".csv";

            br = new BufferedReader(new FileReader(filePath));
            String str[] ;
            Allied allied;
            int colCount=0;
            while ((sCurrentLine = br.readLine()) != null) {
                colCount=0;
                allied = new Allied();
                str = sCurrentLine.split("\t");
                allied.setId(Integer.parseInt(str[colCount++]));
                allied.setName(str[colCount++]);
                allied.setAlliedDesc(str[colCount++]);

                if(str[colCount].equals("null")){
                    colCount++;
                }else{
                    allied.setAlliedImageId(Integer.parseInt(str[colCount++]));
                }

                if(str[colCount].equals("null")){
                    colCount++;
                }else{
                    allied.setOriginalPulseId(Integer.parseInt(str[colCount++]));
                }

                if(str[colCount].equals("null")){
                    colCount++;
                }else{
                    allied.setDateOfBirth(formatter.parse(str[colCount++]));
                }

                alliedDAO.restoreAllied(allied);
                //System.out.println(allied);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null)br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }


    public void restore() {
        BufferedReader br = null;
        Scanner scan = null;
        try {
            String sCurrentLine;
            File folder = Environment.getExternalStoragePublicDirectory(
                    context.getString(R.string.externalDirectoryPath_appFolderName) +
                    File.separator +
                    context.getString(R.string.externalDirectoryPath_Backup)+
                    File.separator +
                    context.getString(R.string.externalDirectoryPath_Database));

                    //context.getString(R.string.externalDirectoryDatabasePath));
            //storageDir.mkdirs();
            String filePath = folder.toString() + File.separator + DataBaseHelper.ALLIED_TABLE+".csv";

            //br = new BufferedReader(new FileReader(filePath));
            String str[] ;
            Allied allied;
            int colCount=0;
            scan = new Scanner(new File(filePath));
            //while ((sCurrentLine = br.readLine()) != null) {
            while(scan.hasNext()){
                sCurrentLine = scan.nextLine();
                colCount=0;
                allied = new Allied();
                str = sCurrentLine.split("\t");
                allied.setId(Integer.parseInt(str[colCount++]));
                allied.setName(str[colCount++]);
                allied.setAlliedDesc(str[colCount++]);

                if(str[colCount].equals("null")){
                    colCount++;
                }else{
                    allied.setAlliedImageId(Integer.parseInt(str[colCount++]));
                }

                if(str[colCount].equals("null")){
                    colCount++;
                }else{
                    allied.setOriginalPulseId(Integer.parseInt(str[colCount++]));
                }

                if(str[colCount].equals("null")){
                    colCount++;
                }else{
                    allied.setDateOfBirth(formatter.parse(str[colCount++]));
                }

                alliedDAO.restoreAllied(allied);
                //System.out.println(allied);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (scan != null)scan.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

    public void deleteAllRecords()
    {
        alliedDAO.deleteAllRecords();
    }


}
