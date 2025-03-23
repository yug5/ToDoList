package com.example.todolist

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.databinding.ActivityMainBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var database: DatabaseReference
    private var user_task: ArrayList<tasks_item> = ArrayList()
    private var taskKeys: ArrayList<String> = ArrayList()
    private lateinit var adapter: Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        databaseReference = FirebaseDatabase.getInstance().reference
        setContentView(binding.root)

        // Initialize RecyclerView
        binding.ryc.layoutManager = LinearLayoutManager(this)
        binding.ryc.setHasFixedSize(true)
        adapter = Adapter(user_task, taskKeys)
        binding.ryc.adapter = adapter

        // Swipe to delete
        val swipe = object : SwipeGesture(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                when (direction) {

                    ItemTouchHelper.LEFT -> {
                        val position = viewHolder.absoluteAdapterPosition
                        val taskKey = adapter.taskKeys[position]
                        adapter.delete(position)
                        databaseReference.child("Tasks").child(taskKey).removeValue()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(this@MainActivity, "Task removed successfully", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this@MainActivity, "Failed to remove task", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                }
            }
        }
        val touch = ItemTouchHelper(swipe)
        touch.attachToRecyclerView(binding.ryc)

        // For data from Firebase
        database = FirebaseDatabase.getInstance().getReference("Tasks")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                user_task.clear() // Clear the list to avoid duplicates
                taskKeys.clear()
                if (snapshot.exists()) {
                    for (dataSnap in snapshot.children) {
                        val user = dataSnap.getValue(tasks_item::class.java)
                        if (user != null && !user_task.contains(user)) {
                            user_task.add(user)
                            taskKeys.add(dataSnap.key!!)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, error.toString(), Toast.LENGTH_LONG).show()
            }
        })

        val ad = findViewById<Button>(R.id.add)
        ad.setOnClickListener {
            addtask()
        }
    }

    private fun addtask() {
        val d = Dialog(this)
        d.requestWindowFeature(Window.FEATURE_NO_TITLE)
        d.setCancelable(true)
        d.setContentView(R.layout.activity_add_task)
        d.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val sav = d.findViewById<Button>(R.id.save)
        sav.setOnClickListener {
            val ta = d.findViewById<TextInputEditText>(R.id.task).text.toString()

            if (ta.isEmpty()) {
                Toast.makeText(this, "Enter the Task..", Toast.LENGTH_LONG).show()
            } else {
                val key = databaseReference.child("Tasks").push().key

                val tasksitem = tasks_item(ta, false)

                if (tasksitem != null && key != null) {
                    databaseReference.child("Tasks").child(key).setValue(tasksitem)
                        .addOnCompleteListener { tas ->
                            if (tas.isSuccessful) {
                                Toast.makeText(this, "Saved", Toast.LENGTH_LONG).show()
                                user_task.distinct()
                                d.dismiss()
                            } else {
                                Toast.makeText(this, "Failed to save", Toast.LENGTH_LONG).show()
                                d.dismiss()
                            }
                        }
                } else {
                    Toast.makeText(this, "Failed to generate key or task item is null", Toast.LENGTH_LONG).show()
                }
            }
            d.dismiss()
        }
        d.show()
    }
}
