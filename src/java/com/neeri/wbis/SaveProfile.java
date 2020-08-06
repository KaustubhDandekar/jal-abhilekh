package com.neeri.wbis;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.neeri.wbis.database.DatabaseHelper;

import static com.neeri.wbis.database.DataBaseConstants.U_CONTACT;
import static com.neeri.wbis.database.DataBaseConstants.U_EMAIL;
import static com.neeri.wbis.database.DataBaseConstants.U_NAME;

public class SaveProfile extends AppCompatActivity {

    EditText name, email, contact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_profile);

        name = findViewById(R.id.profile_name);
        email = findViewById(R.id.profile_email);
        contact = findViewById(R.id.profile_contact);

        fillProfile();


    }

    private void fillProfile(){
        Cursor cr = new DatabaseHelper(this).getUserInfo();
        cr.moveToNext();
        name.setText(cr.getString(cr.getColumnIndex(U_NAME)));
        email.setText(cr.getString(cr.getColumnIndex(U_EMAIL)));
        contact.setText(cr.getString(cr.getColumnIndex(U_CONTACT)));
    }

    public void updateProfile(View view){
        String u_name = name.getText().toString();
        String u_contact = contact.getText().toString();
        String u_email = email.getText().toString();

        boolean updated = (new DatabaseHelper(this)).updateUserInfo(u_name, u_email, u_contact);
        if(updated){
            Toast.makeText(this, "Profile Details updated Successfully", Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(this, "ERROR", Toast.LENGTH_LONG).show();
        }
        finish();
    }

    public void back(View view){
        finish();
    }
}
