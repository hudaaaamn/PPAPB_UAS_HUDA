package com.example.ppapb_uas

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ppapb_uas.database.Note
import com.example.ppapb_uas.database.NoteDao
import com.example.ppapb_uas.database.NoteRoomDatabase
import com.example.ppapb_uas.databinding.FragmentUserListBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [UserListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserListFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding : FragmentUserListBinding
    private lateinit var database : DatabaseReference
    private lateinit var recyclerViewItem : RecyclerView
    private lateinit var itemAdapter : RecyclerViewAdapterUser
//    private lateinit var itemList : ArrayList<Note>

    private lateinit var dao: NoteDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserListBinding.inflate(layoutInflater)

        recyclerViewItem = binding.userRecyclerView
        recyclerViewItem.setHasFixedSize(true)
        recyclerViewItem.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL,false)

        itemAdapter = RecyclerViewAdapterUser(emptyList())
        recyclerViewItem.adapter = itemAdapter

        // Initialize Room database
        dao = NoteRoomDatabase.getDatabase(requireContext()).dao()

        // Initialize Firebase reference
        database = FirebaseDatabase.getInstance().getReference("Admin")

        // Fetch data from Firebase and update itemList
        fetchFilmFromFirebase()


        return binding.root
    }


    private fun fetchFilmFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val filmList = mutableListOf<Note>()

                for (dataSnapshot in snapshot.children) {
                    val uid = dataSnapshot.key ?: ""
                    val filmEntity = dataSnapshot.getValue(Note::class.java)

                    filmEntity?.let {
                        // Set properti id dengan nilai UID
                        it.id = uid
                        filmList.add(it)
                    }
                }

                // Delete all data in Room database
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        dao.deleteAllFilm()
                        Log.d("Delete", "All films deleted")
                    }
                }

                // Update Room database with the new data from Firebase
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        // Insert the entire list without clearing existing data
                        dao.insertFilm(filmList)
                        Log.d("Insert", "Number of films inserted: ${filmList.size}")
                    }
                }

                // Observe changes in the LiveData from Room and update the adapter
                dao.getAllFilm().observe(viewLifecycleOwner, Observer { films ->
                    Log.d("Update Data", "Number of films updated: ${films.size}")
                    // Ensure this operation is done on the UI thread
                    itemAdapter.updateData(films)
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FetchData", "Failed to fetch data from Firebase: ${error.message}")
                // Handle the error if needed
            }
        })
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment UserListFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UserListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}