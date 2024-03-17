package com.example.ekichabi.ui.main;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.BitSet;

import java.util.Base64;

public class AndroidLogger {
    private String filename;
    public static final int MAXIMUM_ACTION_BITS = 128;

    public AndroidLogger(String filename) {
        this.filename = filename;
    }

    public AndroidLogger() {
        this("log.bin");
    }

    public String getFilename() {
        return filename;
    }

    public String toJSON(Context context, byte[] bytes) {
        // getting base64 encoded string bytes
        byte[] bytesEncoded = Base64.getEncoder().encode(bytes);
        byte[] decoded = Base64.getDecoder().decode(bytesEncoded);
        // composing json
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        String phoneNum = preferences.getString("mphoneNum", "-1");
        String json = "{\"phone_num\":\"" + phoneNum + "\",\"loggedData\":\""+ new String(bytesEncoded) +"\"}";
//        Log.i("log", "log json: " + json);
        return json;
    }

    public BitSet toBitSet(int n, int bits) {
        BitSet data = new BitSet(bits);
        for (int i = 0; i < bits; i++) {
            data.set(i, getBit(n, i));
        }
        return data;
    }

    // function to get file content to send it in a POST request to the server
//     public String readFileContent(Context context) throws IOException {
//         FileInputStream fin = context.getApplicationContext().openFileInput(filename);
//         int c;
//         StringBuilder temp = new StringBuilder();
//         while ((c = fin.read()) != -1) {
//             temp.append(Character.toString((char) c));
//         }
//         fin.close();
//         return temp.toString();
//     }

    public byte[] readBytes(Context context) throws IOException {
//        FileInputStream fin = context.openFileInput(filename);
//        Log.i("readBytes()", "num1: " + Arrays.toString(context.getApplicationContext().getFilesDir().listFiles()));
        byte[] bFile = new byte[(int) new File(String.valueOf(context.getApplicationContext().getFileStreamPath(filename))).length()];
        try {
            FileInputStream fin = context.getApplicationContext().openFileInput(filename);
            int a = fin.read(bFile);
//            Log.i("readBytes()", "num2: " + a);
            fin.close();
        } catch (FileNotFoundException ex) {
//            Log.i("readBytes()", "");
        }
        return bFile;
    }

//     delete file after POST was successful
     public void deleteFileContent(Context context) {
         if (context.getApplicationContext().deleteFile(filename)) {
             Log.i("delete", "Deleted the file: " + filename);
         } else {
             Log.i("delete", "Failed to delete the file.");
         }
     }

     public int fileSize(Context context) {
        return (int) new File(String.valueOf(context.getApplicationContext().getFileStreamPath(filename))).length();
     }

    // write an array of bytes representing an action to the log file
     public void writeBytesToFile(byte[] bytes, Context context) throws IOException {
         FileOutputStream outputStream = context.getApplicationContext().openFileOutput(filename, Context.MODE_APPEND);
         int file_size = Integer.parseInt(String.valueOf((int) new File(String.valueOf(context.getApplicationContext().getFileStreamPath(filename))).length()/1024));
         if(file_size < 100) {
             outputStream.write(bytes);
             outputStream.close();
         }
         JobScheduler jobScheduler = (JobScheduler) context.getApplicationContext()
                 .getSystemService(context.getApplicationContext().JOB_SCHEDULER_SERVICE);
         ComponentName name = new ComponentName(context.getApplicationContext(), Authentication.LogUpdateJobService.class);
         JobInfo jobInfo = new JobInfo.Builder(0, name)
                 .setPersisted(true).build();
         jobScheduler.schedule(jobInfo);
     }

    public long dayCount() {
//        Log.i("dayCount()", "" + ChronoUnit.DAYS.between(LocalDate.of(2022, 1, 1), LocalDate.now()));
        return ChronoUnit.DAYS.between(LocalDate.of(2022, 1, 1), LocalDate.now());
    }

    /**
     * Get the encoded bytes for when the user favorites a business.
     * @param businessPK the pk (Primary Key) value in the android json for the business. Must be less than 65K (2**15 - 1)
     * @return the encoded bytes for a favorite action
     */
    public byte[] getFavoriteAction(long businessPK) {
        return encodedAction("FAV", dayCount(), BitSet.valueOf(new long[] {businessPK}), 16);
    }

    /**
     * Get the encoded bytes for when the user unfavorites a business.
     * @param businessPK the pk (Primary Key) value in the android json for the business. Must be less than 65K (2**15 - 1)
     * @return the encoded bytes for an unfavorite action
     */
    public byte[] getUnFavoriteAction(long businessPK) {
        return encodedAction("UNF", dayCount(), BitSet.valueOf(new long[] {businessPK}), 16);
    }

    /**
     * Get the encoded bytes for when the user calls a business.
     * @param businessPK the pk (Primary Key) value in the android json for the business. Must be less than 65K (2**15 - 1)
     * @return the encoded bytes for a call action
     */
    public byte[] getCallAction(long businessPK) {
        return encodedAction("CAL", dayCount(), BitSet.valueOf(new long[] {businessPK}), 16);
    }

    /**
     * Get the encoded bytes for when the user opens a business.
     * @param businessPK the pk (Primary Key) value in the android json for the business. Must be less than 65K (2**15 - 1)
     * @return the encoded bytes for a business screen action
     */
    public byte[] getOpenBusinessScreenAction(long businessPK) {
        return encodedAction("BUS", dayCount(), BitSet.valueOf(new long[] {businessPK}), 16);
    }

    /**
     * Get the encoded bytes for when the user adds a business as a contact.
     * @param businessPK the pk (Primary Key) value in the android json for the business. Must be less than 65K (2**15 - 1)
     * @return the encoded bytes for a contact action
     */
    public byte[] getContactAction(long businessPK) {
        return encodedAction("CON", dayCount(), BitSet.valueOf(new long[] {businessPK}), 16);
    }


    /**
     * For when the user searches a string
     * @param cleanedsearchstr must only contain lowercase alpabetical characters
     * @param successful true if the search was successful (user clicked a business, etc.), false if unsuccessful (userexited app, left search screen, made a new search, etc.)
     * @param removednonalphabetical true if non alphabetical characters had to be removed
     * @return the encoded bytes for a search action
     */
    public byte[] getSearchAction(String cleanedsearchstr, boolean successful, boolean removednonalphabetical) {
        int size = Math.min(MAXIMUM_ACTION_BITS - 20, cleanedsearchstr.length()*5 + 3);
        BitSet data = new BitSet(size);
        boolean truncated = size > cleanedsearchstr.length()*5 + 3;
        data.set(0, successful);
        data.set(1, truncated);
        data.set(2, removednonalphabetical);
        for (int i = 3; i < size; i += 5) {
            int value = cleanedsearchstr.charAt((i - 3) / 5) - 'a' + 1;
            for (int j = 0; j < 5; j++) {
                data.set(i + j, getBit(value, j));
            }
        }
        return encodedAction("SEA", dayCount(), data, size);
    }

    public byte[] getFilterAction(String cleanedfilterstr, boolean successful, boolean removednonalphabetical) {
        int size = Math.min(MAXIMUM_ACTION_BITS - 20, cleanedfilterstr.length()*5 + 3);
        BitSet data = new BitSet(size);
        boolean truncated = size > cleanedfilterstr.length()*5 + 3;
        data.set(0, successful);
        data.set(1, truncated);
        data.set(2, removednonalphabetical);
        for (int i = 3; i < size; i += 5) {
            int value = cleanedfilterstr.charAt((i - 3) / 5) - 'a' + 1;
            for (int j = 0; j < 5; j++) {
                data.set(i + j, getBit(value, j));
            }
        }
        return encodedAction("FIL", dayCount(), data, size);
    }

    /**
     * Return the byte[] representation of an action given its type, timestamp and data.
     * @param actionType the action type as a 3 letter string
     * @param modifiedJulianCount the timestamp as a count of days since julianday number JD_OFFSET
     * @param data the bits that constitute the data
     * @param datalength the amount of bits to use from data (countring from the back)
     * @return the bits for the action, padded with zeros at the end to make even bytes
     */
    public byte[] encodedAction(String actionType, long modifiedJulianCount, BitSet data, int datalength) {
        BitSet action = new BitSet(15 + datalength);
        if (actionType.equals("FAV")) { action.set(0, false); action.set(1, false); action.set(2, true) ; }
        else if (actionType.equals("UNF")) { action.set(0, false); action.set(1, true) ; action.set(2, false); }
        else if (actionType.equals("CAL")) { action.set(0, false); action.set(1, true) ; action.set(2, true) ; }
        else if (actionType.equals("BUS")) { action.set(0, true) ; action.set(1, false); action.set(2, false); }
        else if (actionType.equals("CON")) { action.set(0, true) ; action.set(1, false); action.set(2, true) ; }
        else if (actionType.equals("SEA")) { action.set(0, true) ; action.set(1, true) ; action.set(2, false); }
        else if (actionType.equals("FIL")) { action.set(0, true) ; action.set(1, true) ; action.set(2, true) ; }
        for (int i = 0; i < 12; i++) {
            action.set(i + 3, getBit(modifiedJulianCount, i));
        }
        for (int i = 0; i < datalength; i++) {
            action.set(i + 15, data.get(i));
        }
        byte[] result = new byte[(int)Math.ceil((15 + datalength + 8)/8.0)];
        byte[] partial = action.toByteArray();
        for (int i = 0; i < partial.length; i++) {
            // System.out.print(action.get(i) ? 1 : 0);
            result[i] = partial[i];
        }
        // System.out.println();
        return result;
    }

    /**
     * Gets a specific bit in an integer.
     * @param n the integer to get a bit from
     * @param indexfromback the index of the bit to get (counting from the back)
     * @return true if the bit is 1, false otherwise
     */
    public boolean getBit(int n, int indexfromback) {
        return ((n >> indexfromback) & 1) == 1;
    }

    public boolean getBit(long n, int indexfromback) {
        return ((n >> indexfromback) & 1) == 1;
    }
}
