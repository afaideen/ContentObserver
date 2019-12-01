package com.birdpeek.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "debug";
    private int mContactCount;
    private int count;
    private boolean contactService;
    private TextView outputText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        outputText = (TextView)findViewById(R.id.outputText);
        count = getContactCount();
        mContactCount = count;
//        this.getContentResolver().registerContentObserver(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, true, mObserver);
        this.getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, mObserver);
    }

    private int getContactCount() {
        Cursor cursor = null;
        try {
//            cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
            cursor = this.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
            if (cursor != null) {
//                showContact(cursor);
                return cursor.getCount();
            } else {
                cursor.close();
                return 0;
            }
        } catch (Exception ignore) {
        } finally {
            if(cursor!=null)
                cursor.close();
        }
        return 0;
    }

    private void showContact(Cursor cursor) {
        String phoneNumber = null;
        String email = null;

        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        String _ID = ContactsContract.Contacts._ID;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;

        Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;

        Uri EmailCONTENT_URI =  ContactsContract.CommonDataKinds.Email.CONTENT_URI;
        String EmailCONTACT_ID = ContactsContract.CommonDataKinds.Email.CONTACT_ID;
        String DATA = ContactsContract.CommonDataKinds.Email.DATA;

        StringBuffer output = new StringBuffer();

        ContentResolver contentResolver = getContentResolver();
        // Loop for every contact in the phone
        if (cursor.getCount() > 0) {

            while (cursor.moveToNext()) {

                String contact_id = cursor.getString(cursor.getColumnIndex( _ID ));
                String name = cursor.getString(cursor.getColumnIndex( DISPLAY_NAME ));

                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex( HAS_PHONE_NUMBER )));

                if (hasPhoneNumber > 0) {

                    output.append("\n First Name:" + name);

                    // Query and loop for every phone number of the contact
                    Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[] { contact_id }, null);

                    while (phoneCursor.moveToNext()) {
                        phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
                        output.append("\n Phone number:" + phoneNumber);

                    }

                    phoneCursor.close();

                    // Query and loop for every email of the contact
                    Cursor emailCursor = contentResolver.query(EmailCONTENT_URI,    null, EmailCONTACT_ID+ " = ?", new String[] { contact_id }, null);

                    while (emailCursor.moveToNext()) {

                        email = emailCursor.getString(emailCursor.getColumnIndex(DATA));

                        output.append("\nEmail:" + email);

                    }

                    emailCursor.close();
                }

                output.append("\n");
            }

            outputText.setText(output);
        }
    }

    private ContentObserver mObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            this.onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            new changeInContact().execute();
        }
    };

    private class changeInContact extends AsyncTask<String,Void,String> {
        String name = null, id = null, phoneNumber = null;

        @Override
        protected String doInBackground(String... arg0) {
            ArrayList<Integer> arrayListContactID = new ArrayList<Integer>();

            int currentCount = getContactCount();


            if (currentCount > mContactCount) {
                // Contact Added
                Log.d("In","Add");
                final String WHERE_MODIFIED = "( "+ ContactsContract.RawContacts.DELETED + "=1 OR "+ ContactsContract.RawContacts.DIRTY + "=1 )";
                Cursor c = getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI,

                        null,
                        "( dirty=1 )",
                        null,
                        null);
                if (c.getCount() > 0) {

                    c.moveToLast();
                    name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    id = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
                    Log.d(TAG, "name: " + name);
                    Log.d(TAG, "id: " + id);
                    ArrayList<String> phones = new ArrayList<String>();

                    Cursor cursor = getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                            new String[]{id}, null);

                    while (cursor.moveToNext())
                    {
                        phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        phones.add(phoneNumber);
                        Log.d(TAG, "Phone Number: " + phoneNumber);
                    }

                    cursor.close();

                }
            } else if (currentCount < mContactCount) {
                // Delete Contact
                Log.d("In","Delete");

                final String WHERE_MODIFIED = "( "+ ContactsContract.RawContacts.DELETED + "=1 OR "+ ContactsContract.RawContacts.DIRTY + "=1 )";
                Cursor c = getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI,
                        null,
                        "( deleted=1 )",
                        null,
                        null);
                if (c.getCount() > 0) {

                    c.moveToLast();
                    name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    id = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
                    Log.d(TAG, "name: " + name);
                    Log.d(TAG, "id: " + id);
                    ArrayList<String> phones = new ArrayList<String>();

                    Cursor cursor = getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                            new String[]{id}, null);

                    while (cursor.moveToNext())
                    {
                        phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        phones.add(phoneNumber);
                        Log.d(TAG, "Phone Number: " + phoneNumber);
                    }

                    cursor.close();


                }
            } else if (currentCount == mContactCount) {
                // Update Contact
                Log.d("In","Update");
                final String WHERE_MODIFIED = "( "+ ContactsContract.RawContacts.DELETED + "=1 OR "+ ContactsContract.RawContacts.DIRTY + "=1 )";
                Cursor c = getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, null, "( dirty=1 )", null, null);
//                Cursor c = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
                if (c.getCount() > 0) {

                    c.moveToLast();
                    name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    id = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
                    Log.d(TAG, "name: " + name);
                    Log.d(TAG, "id: " + id);
                    ArrayList<String> phones = new ArrayList<String>();

                    Cursor cursor = getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                            new String[]{id}, null);

                    while (cursor.moveToNext())
                    {
                        phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        phones.add(phoneNumber);
                        Log.d(TAG, "Phone Number: " + phoneNumber);
                    }

                    cursor.close();


                }
            }
            mContactCount = currentCount;
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            contactService = false;
        } // End of post
    }
}

