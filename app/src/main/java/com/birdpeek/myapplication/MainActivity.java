package com.birdpeek.myapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.ListIterator;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "#debug";
    private int mContactCount;
    private int count;
    private boolean contactService;
    private TextView outputText;
    private Calendar now;
    public static final int PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private List<MyContact> listContacts, listDeletedContact, listAddedOrUpdatedContact;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        outputText = (TextView)findViewById(R.id.outputText);
        requestContactPermission();


    }

    private void getContacts() {

        Toast.makeText(this, "Get contacts ....", Toast.LENGTH_LONG).show();
        Cursor cursor = this.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        showContact(cursor);
        count = getContactCount();
        mContactCount = count;
        this.getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, mObserver);

    }

    public void requestContactPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.READ_CONTACTS)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Read Contacts permission");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setMessage("Please enable access to contacts.");
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @TargetApi(Build.VERSION_CODES.M)
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            requestPermissions(
                                    new String[]
                                            {android.Manifest.permission.READ_CONTACTS}
                                    , PERMISSIONS_REQUEST_READ_CONTACTS);
                        }
                    });
                    builder.show();
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.READ_CONTACTS},
                            PERMISSIONS_REQUEST_READ_CONTACTS);
                }
            } else {
                getContacts();
            }
        } else {
            getContacts();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_CONTACTS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContacts();
                } else {
                    Toast.makeText(this, "You have disabled a contacts permission", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    private int getContactCount() {
        Cursor cursor = null;
        try {
            cursor = this.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
            if (cursor != null) {
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
        listContacts = new ArrayList<>();
        // Loop for every contact in the phone
        if (cursor.getCount() > 0) {

            while (cursor.moveToNext()) {

                String contact_id = cursor.getString(cursor.getColumnIndex( _ID ));
                String name = cursor.getString(cursor.getColumnIndex( DISPLAY_NAME ));

                MyContact contact = new MyContact();
                contact._id = contact_id;
                contact.name = name;
                listContacts.add(contact);

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
            Log.d(TAG, "Show contacts, listContacts size: " + listContacts.size());
        }
    }

    private ContentObserver mObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
//            this.onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            new changeInContact().execute();
        }
    };

    private class changeInContact extends AsyncTask<String,Void,String> {
        String name = null, id = null, contact_id = null, phoneNumber = null, contact_deleted_timestamp=null;
        private Timestamp time;


        @Override
        protected String doInBackground(String... arg0) {
            ArrayList<Integer> arrayListContactID = new ArrayList<Integer>();

            int currentCount = getContactCount();


            if (currentCount >= mContactCount) {
                // Contact Added
                if(currentCount > mContactCount)
                    Log.d(TAG,"Add");
                else
                    Log.d(TAG,"Update");
                listAddedOrUpdatedContact = queryAddedOrUpdatedContact();
                if(currentCount > mContactCount) {
                    //only for add operation
                    listContacts.addAll(listAddedOrUpdatedContact);//add
                }


                Log.d(TAG, "listContacts size: " + listContacts.size());
            } else if (currentCount < mContactCount) {
                // Delete Contact
                Log.d(TAG,"Delete");
                listDeletedContact = queryDeletedContact();
                listContacts = updateListContacts(listContacts, listDeletedContact);

                Log.d(TAG, "listContacts size: " + listContacts.size());


            }
            mContactCount = currentCount;

            return "";
        }

        private List<MyContact> updateListContacts(List<MyContact> list, List<MyContact> listDeletedContact) {
            String id;
            for (MyContact deletedContact:listDeletedContact) {
                id = deletedContact._id;
                ListIterator<MyContact> iter = list.listIterator();
                while(iter.hasNext()){
                    if(iter.next()._id.equals(id)){
                        Log.d(TAG, "id: " + id + " Remove " + name + " from listContacts");
                        iter.remove();
                        break;
                    }
                }

            }
            return list;
        }

        private List<MyContact> queryAddedOrUpdatedContact() {
            List<MyContact> list = new ArrayList<>();
            //                final String WHERE_MODIFIED = "( "+ ContactsContract.RawContacts.DELETED + "=1 OR "+ ContactsContract.RawContacts.DIRTY + "=1 )";
            Cursor c = getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI,
                    null,
                    "( dirty=1 )",
                    null,
                    null);
            while (c.moveToNext()){
                name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                id = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
                Log.d(TAG, "id: " + id + " name: " + name);
                ArrayList<String> listPhoneNum = queryListPhoneNum(id);
                MyContact contact = new MyContact(id,name,listPhoneNum);
                list.add(contact);
            }
            c.close();
            return list;
        }

        private List<MyContact> queryDeletedContact() {
            List<MyContact> list = new ArrayList<>();
            Cursor c = getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, null, "deleted = 1", null, null);
            while(c.moveToNext()){

                id = c.getString(c.getColumnIndex("_id"));
                name = c.getString(c.getColumnIndex("display_name"));
                time = new Timestamp(0);
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
//                            Cursor c1 = getContentResolver().query(ContactsContract.DeletedContacts.CONTENT_URI, null, "contact_id=?", new String[]{id}, null);
                    Cursor c1 = getContentResolver().query(ContactsContract.DeletedContacts.CONTENT_URI, null, "contact_id="+id, null, null);
                    c1.moveToNext();
                    contact_id = c1.getString(c1.getColumnIndex("contact_id"));
                    contact_deleted_timestamp = c1.getString(c1.getColumnIndex("contact_deleted_timestamp"));
                    time = new Timestamp(Long.valueOf(contact_deleted_timestamp));

                }
                Log.d(TAG, "id: " + id + " name: " + name + " time: " + time.toString());
                MyContact deletedContact = new MyContact(id, name, contact_deleted_timestamp);
                list.add(deletedContact);
            }
            c.close();
            return list;
        }

        private ArrayList<String> queryListPhoneNum(String id) {
            ArrayList<String> phones = new ArrayList<String>();

//            Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, "contact_id = ?", new String[]{id}, null);
            Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, "contact_id = " + id, null, null);

            while (cursor.moveToNext())
            {
                phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                phones.add(phoneNumber);
                Log.d(TAG, "Phone Number: " + phoneNumber);
            }
            cursor.close();
            return phones;
        }

        @Override
        protected void onPostExecute(String result) {
            contactService = false;
        } // End of post
    }

    private class MyContact {
        private ArrayList<String> listPhoneNum;
        private String _id,name,contact_deleted_timestamp;

        public MyContact() {
        }

        public MyContact(String id, String name) {
            _id = id;
            this.name = name;
        }

        public MyContact(String id, String name, String contact_deleted_timestamp) {
            _id = id;
            this.name = name;
            this.contact_deleted_timestamp = contact_deleted_timestamp;

        }

        public MyContact(String id, String name, ArrayList<String> listPhoneNum) {
            _id = id;
            this.name = name;
            this.listPhoneNum = listPhoneNum;
        }
    }
}

