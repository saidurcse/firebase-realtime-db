package com.demorealtimedb

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage


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

    private lateinit var chooseImg: Button
    private lateinit var uploadImg:Button
    private lateinit var imgView: ImageView
    var PICK_IMAGE_REQUEST = 111
    var filePath: Uri? = null
    private lateinit var pd: ProgressDialog

    //creating reference to firebase storage
    var storage = FirebaseStorage.getInstance()
    var storageRef =
        storage.getReferenceFromUrl("gs://androidlive-66b3c.appspot.com") //change the url according to your firebase app


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

        chooseImg = findViewById(R.id.chooseImg)
        uploadImg = findViewById(R.id.uploadImg)
        imgView = findViewById(R.id.imgView)

        pd = ProgressDialog(this)
        pd!!.setMessage("Uploading....")


        chooseImg!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_PICK
                startActivityForResult(
                    Intent.createChooser(intent, "Select Image"),
                    PICK_IMAGE_REQUEST
                )
            }
        })

        uploadImg!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (filePath != null) {
                    pd!!.show()
                    val childRef = storageRef.child("image.jpg")
                    //uploading the image
                    val uploadTask = childRef.putFile(filePath!!)
                    uploadTask.addOnSuccessListener {
                        pd!!.dismiss()
                        Toast.makeText(this@MainActivity,"Upload successful",Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener { e ->
                        pd!!.dismiss()
                        Toast.makeText(this@MainActivity,"Upload Failed -> $e",Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Select an image", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            filePath = data.data
            try { //getting image from gallery
                val bitmap =
                    MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                //Setting image to ImageView
                imgView!!.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
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
