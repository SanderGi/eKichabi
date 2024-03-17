
package resources;

import java.util.BitSet;
import java.util.Random;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.File;

import java.util.Base64;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

class AndroidLogger {
    private String filename;
    public static final int MAXIMUM_ACTION_BITS = 128;

    public AndroidLogger(String filename) {
        this.filename = filename;
    }

    public AndroidLogger() {
        this("resources/log.bin");
    }

    public String getFilename() {
        return filename;
    }

    public String getRandomString(Random rnd, int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toLowerCase();
        StringBuilder str = new StringBuilder();
        while (str.length() < length) { // length of the random string.
            int index = (int) (rnd.nextFloat() * chars.length());
            str.append(chars.charAt(index));
        }
        String result = str.toString();
        return result;

    }

    // testing
    public static void main(String[] args) throws FileNotFoundException, IOException {
        AndroidLogger a = new AndroidLogger();
        Random rand = new Random();
        String[] filterOptions = new String[] {"futawilaya","kyerwa","muleba","missenyi","karagwe","bukobacbd","bukoba","futasekta","biasharanakuuzajumla","mfanyabiashararejareja","usafiri","usindikajimazao","biasharawenyeujuzi","hudumazisizozakilimo"};

        FileOutputStream fout = new FileOutputStream(a.filename, false);
        fout.write(new byte[0]);
        fout.close();
        fout = new FileOutputStream(a.filename, true);
        for (int i = 0; i < 15; i++) {
            double r = rand.nextDouble();
            if (r < 1.0 / 7.0)
                fout.write(a.getFavoriteAction(rand.nextInt(9500)));
            else if (r < 2.0 / 7.0)
                fout.write(a.getCallAction(rand.nextInt(9500)));
            else if (r < 3.0 / 7.0)
                fout.write(a.getContactAction(rand.nextInt(9500)));
            else if (r < 4.0 / 7.0)
                fout.write(a.getUnFavoriteAction(rand.nextInt(9500)));
            else if (r < 5.0 / 7.0)
                fout.write(a.getOpenBusinessScreenAction(rand.nextInt(9500)));
            else if (r < 6.0 / 7.0)
                fout.write(a.getFilterAction(filterOptions[rand.nextInt(filterOptions.length)], rand.nextBoolean(), true));
            else
                fout.write(a.getSearchAction(a.getRandomString(rand, rand.nextInt(8,30)), rand.nextBoolean(), rand.nextBoolean()));
        }
        fout.close();

        FileInputStream fin = new FileInputStream(a.filename);
        byte[] bytes = new byte[(int)(new File(a.filename)).length()];
        fin.read(bytes);
        fin.close();
        for (byte b : bytes) {
            System.out.println(Integer.toBinaryString(b & 255 | 256).substring(1));
        }
        System.out.println(a.toJSON(""+rand.nextInt(100000000, 999999999), bytes));
        System.out.println((int)a.dayCount());
    }

    public String toJSON(String deviceID, byte[] bytes) {
        // getting base64 encoded string bytes
        byte[] bytesEncoded = Base64.getEncoder().encode(bytes);

        // composing json
        String json = "{\"phone_num\":\"" + deviceID + "\",\"loggedData\":\""+ new String(bytesEncoded) +"\"}";        

        return json;
    }

    public BitSet toBitSet(int n, int bits) {
        BitSet data = new BitSet(bits);
        for (int i = 0; i < bits; i++) {
            data.set(i, getBit(n, i));
        }
        return data;
    }

    // // function to get file content to send it in a POST request to the server
    // public String readFileContent() {
    //     FileInputStream fin = openFileInput(filename);
    //     int c;
    //     String temp = "";
    //     while( (c = fin.read()) != -1){
    //         temp = temp + Character.toString((char)c);
    //     }
    //     fin.close();
    //     return temp;
    // }

    // delete file after POST was successful
    // public deleteFilecontent() {
    //     Context.deleteFile(filename);
    // }

    // write an array of bytes representing an action to the log file
    // public void writeBytesToFile(byte[] bytes) {
    //     FileOutputStream outputStream = getApplicationContext().openFileOutput(filename, Context.MODE_PRIVATE);
    //     outputStream.write(bytes);
    //     outputStream.close();
    // }

    public long dayCount() {
        return ChronoUnit.DAYS.between(LocalDate.of(2022, 1, 1), LocalDate.now());
    }

    /**
     * Get the encoded bytes for when the user favorites a business.
     * @param businessPK the pk (Primary Key) value in the android json for the business. Must be less than 65K (2**15 - 1)
     * @return the encoded bytes for a favorite action
     */
    public byte[] getFavoriteAction(int businessPK) {
        return encodedAction("FAV", dayCount(), toBitSet(businessPK, 16), 16);
    }

    /**
     * Get the encoded bytes for when the user unfavorites a business.
     * @param businessPK the pk (Primary Key) value in the android json for the business. Must be less than 65K (2**15 - 1)
     * @return the encoded bytes for an unfavorite action
     */
    public byte[] getUnFavoriteAction(int businessPK) {
        return encodedAction("UNF", dayCount(), toBitSet(businessPK, 16), 16);
    }

    /**
     * Get the encoded bytes for when the user calls a business.
     * @param businessPK the pk (Primary Key) value in the android json for the business. Must be less than 65K (2**15 - 1)
     * @return the encoded bytes for a call action
     */
    public byte[] getCallAction(int businessPK) {
        return encodedAction("CAL", dayCount(), toBitSet(businessPK, 16), 16);
    }

    /**
     * Get the encoded bytes for when the user opens a business.
     * @param businessPK the pk (Primary Key) value in the android json for the business. Must be less than 65K (2**15 - 1)
     * @return the encoded bytes for a business screen action
     */
    public byte[] getOpenBusinessScreenAction(int businessPK) {
        return encodedAction("BUS", dayCount(), toBitSet(businessPK, 16), 16);
    }

    /**
     * Get the encoded bytes for when the user adds a business as a contact.
     * @param businessPK the pk (Primary Key) value in the android json for the business. Must be less than 65K (2**15 - 1)
     * @return the encoded bytes for a contact action
     */
    public byte[] getContactAction(int businessPK) {
        return encodedAction("CON", dayCount(), toBitSet(businessPK, 16), 16);
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
        boolean truncated = size < cleanedsearchstr.length()*5 + 3;
        System.out.println(cleanedsearchstr + " " + truncated + " " + size + " " + (cleanedsearchstr.length()*5 + 3));
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
        boolean truncated = size < cleanedfilterstr.length()*5 + 3;
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
