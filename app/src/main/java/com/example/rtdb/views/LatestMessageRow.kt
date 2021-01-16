package com.example.rtdb.views

import android.util.Log
import com.example.rtdb.R
import com.example.rtdb.models.ChatMessage
import com.example.rtdb.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.latest_message_row.view.*
import kotlinx.android.synthetic.main.user_row_new_message.view.*

class LatestMessageRow(val chatMessage: ChatMessage): Item<ViewHolder>(){
    var chatPartnerUser :User?=null
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.message_textview_latest_message.text = chatMessage.text
        val chatPartnerId:String
        if(chatMessage.fromId == FirebaseAuth.getInstance().uid){
            chatPartnerId = chatMessage.toId
        }
        else
        {
            chatPartnerId = chatMessage.fromId
        }
        val ref = FirebaseDatabase.getInstance().getReference("users/$chatPartnerId")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatPartnerUser= snapshot.getValue(User::class.java)
                viewHolder.itemView.username_latest_message.text = chatPartnerUser?.username
                val targetImageView = viewHolder.itemView.imageview_latest_message
                if(targetImageView !=null)
                Picasso.get().load(chatPartnerUser?.profileImageUrl).into(targetImageView)
                else
                {
                    Log.d("Picassofail","Picasso failed")
                }
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
        viewHolder.itemView.username_latest_message.text = ""

    }
    override fun getLayout(): Int {
        return R.layout.latest_message_row
    }
}