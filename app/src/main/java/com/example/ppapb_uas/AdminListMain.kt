package com.example.ppapb_uas

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ppapb_uas.databinding.ActivityAdminListMainBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.ArrayList

class AdminListMain : AppCompatActivity() {
    private lateinit var binding : ActivityAdminListMainBinding
    private lateinit var database : DatabaseReference
    private lateinit var itemAdapter : RecyclerViewAdapterAdmin
    private lateinit var itemList : ArrayList<Item>
    private lateinit var recyclerViewItem : RecyclerView

    private lateinit var auth : FirebaseAuth

    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminListMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        binding.adminLogout.setOnClickListener{
            val sharedPreferences = this.getSharedPreferences("user_data", Context.MODE_PRIVATE)
            sharedPreferences.edit().putBoolean("isLoggedIn", false).apply()


            auth.signOut()
            startActivity(Intent(this@AdminListMain,MainActivity::class.java))
        }

        recyclerViewItem = binding.adminRecyclerView
        recyclerViewItem.setHasFixedSize(true)
        recyclerViewItem.layoutManager = LinearLayoutManager(this , LinearLayoutManager.VERTICAL,false)


        itemList = arrayListOf()
        itemAdapter = RecyclerViewAdapterAdmin(itemList)
        recyclerViewItem.adapter = itemAdapter


        with(binding){
            adminAddButton.setOnClickListener{
                startActivity(Intent(this@AdminListMain,AdminListAdd::class.java))
            }
        }

        database = FirebaseDatabase.getInstance().getReference("Admin")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Clear the existing list
                itemList.clear()

                // Iterate through the snapshot and add items to the list
                for (dataSnapshot in snapshot.children) {
                    val item = dataSnapshot.getValue(Item::class.java)
                    if (item != null) {
                        itemList.add(item)
                    }
                }

                // Notify the adapter that the data has changed
                itemAdapter.notifyDataSetChanged()
                Log.d("msg",itemList.size.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors if needed
                Toast.makeText(this@AdminListMain, "Gagal Mengambil Data!", Toast.LENGTH_SHORT).show()
            }
        })


    }
}
