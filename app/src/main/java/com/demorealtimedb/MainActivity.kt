package com.demorealtimedb

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var dbReference: DatabaseReference
    private lateinit var firebaseDatabase: FirebaseDatabase

    private var userId: String = ""

    //TextView
    private lateinit var userNameTv: TextView
    private lateinit var userMobileTv: TextView

    //EditText
    private lateinit var userNameEt: EditText
    private lateinit var userMobileEt: EditText

    //Button
    private lateinit var updateUserBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseDatabase = FirebaseDatabase.getInstance()
        dbReference = firebaseDatabase.getReference("users")

        userNameTv = findViewById(R.id.user_name)
        userMobileTv = findViewById(R.id.user_mobile)

        userNameEt = findViewById(R.id.name_edt_text)
        userMobileEt = findViewById(R.id.mobile_edt_text)

        updateUserBtn = findViewById(R.id.update_user_btn)

        userId = dbReference.push().key.toString()

        updateUserBtn.setOnClickListener{
            var name: String = userNameEt.text.toString()
            var mobile: String = userMobileEt.text.toString()

            if(TextUtils.isEmpty(userId)){
                createUser(name, mobile)
            } else{
                updateUser(name, mobile)
            }
        }
    }

    private fun updateUser(name: String, mobile: String) {

        // updating the user via child nodes
        if (!TextUtils.isEmpty(name))
            dbReference.child(userId).child("name").setValue(name)

        if (!TextUtils.isEmpty(mobile))
            dbReference.child(userId).child("mobile").setValue(mobile)

        addUserChangeListener()

    }

    private fun createUser(name: String, mobile: String) {
        val user = UserInfo(name, mobile)
        dbReference.child(userId).setValue(user)
    }

    private fun addUserChangeListener() {
        // User data change listener
        dbReference.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(UserInfo::class.java)

                // Check for null
                if (user == null) {
                    return
                }


                // Display newly updated name and email
                userNameTv.setText(user?.name).toString()
                userMobileTv.setText(user?.mobile).toString()

                // clear edit text
                userNameEt.setText("")
                userMobileEt.setText("")

                changeButtonText()
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
            }
        })
    }

    private fun changeButtonText(){
        if (TextUtils.isEmpty(userId)) {
            updateUserBtn.text = "Save";
        } else {
            updateUserBtn.text = "Update";
        }
    }
}
