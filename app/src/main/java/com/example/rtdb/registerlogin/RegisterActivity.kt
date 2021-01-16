package com.example.rtdb.registerlogin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.example.rtdb.R
import com.example.rtdb.messages.LatestMessagesActivity
import com.example.rtdb.models.User
import kotlinx.android.synthetic.main.activity_register.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        register_button_reg.setOnClickListener {
            performRegister()
        }
        already_have_account_textView.setOnClickListener {
            Log.d("MainActivitytest","Try to show login activity")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        button_image_select_reg.setOnClickListener {
            Log.d("RegisterActivity","Try to show image picker")
            val intent= Intent(Intent.ACTION_PICK)
            intent.type="image/*"
            startActivityForResult(intent,0)
        }
    }
    var selectedPhotoUri: Uri?=null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode== 0 && resultCode== Activity.RESULT_OK && data!=null){
            // check selected image
            Log.d("RegisterActivity","Photo was selected")
            selectedPhotoUri=data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,selectedPhotoUri)
            button_image_select_circleview.setImageBitmap(bitmap)
            button_image_select_reg.alpha=0f
//            val bitmapDrawable= BitmapDrawable(bitmap)
//            button_image_select_reg.setBackgroundDrawable(bitmapDrawable)
        }

    }
    private fun performRegister(){
        val email= email_edittext_reg.text.toString()
        val password = password_edittext_reg.text.toString()
        if(email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this,"Please Enter Valid Details", Toast.LENGTH_SHORT).show()
            return
        }
        Log.d("MainActivitytest", "E-mail is : "+ email)
        Log.d("MainActivitytest","Password : $password")
        //Firebase Authentication
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener{
                if(!it.isSuccessful)
                {
                    return@addOnCompleteListener
                }

                // else if succesful
                Log.d("RegisterActivity","Created User ${it.result?.user?.uid}")
                uploadImageToFirebaseStorage()
            }
            .addOnFailureListener{
                Log.d("RegisterActivity", "failed ${it.message}")
                Toast.makeText(this,"${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun uploadImageToFirebaseStorage(){
        //Log.d("test","$selectedPhotoUri")
        if(selectedPhotoUri==null) return
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("trail", "successfully uploaded image ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {
                    Log.d("trail", "File Location: $it")
                    saveUsertoFirebaseDatabase(it.toString())
                }
            }
            .addOnFailureListener{
                Log.d("trail","failed to upload photo")
            }
    }
    private fun saveUsertoFirebaseDatabase(profileImageUrl: String){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val database =  Firebase.database
        val ref= database.getReference("/users/$uid")
        val user= User(uid,username_edittext_reg.text.toString(),profileImageUrl)
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("trail","added user successfully")
                val intent = Intent(this, LatestMessagesActivity::class.java)
                intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }

    }
}
