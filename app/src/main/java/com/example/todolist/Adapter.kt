package com.example.todolist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.databinding.TasksLayoutBinding
import com.google.firebase.database.FirebaseDatabase

class Adapter(private val tasks: ArrayList<tasks_item>, val taskKeys: ArrayList<String>) : RecyclerView.Adapter<Adapter.UserTasks>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserTasks {
        val itemBinding = TasksLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserTasks(itemBinding)
    }

    override fun onBindViewHolder(holder: UserTasks, position: Int) {
        holder.bind(tasks[position], taskKeys[position])
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    fun delete(position: Int) {
        tasks.removeAt(position)
        taskKeys.removeAt(position)
        notifyItemRemoved(position)
    }

    inner class UserTasks(val binding: TasksLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: tasks_item, taskKey: String) {
            binding.ch.text = task.task
            binding.ch.isChecked = task.isChecked

            binding.ch.setOnCheckedChangeListener { _, isChecked ->
                task.isChecked = isChecked
                FirebaseDatabase.getInstance().getReference("Tasks").child(taskKey).setValue(task)
            }
            if (task.isChecked) {
                binding.ch.setTextColor(ContextCompat.getColor(binding.root.context, R.color.cha))
            } else {
                binding.ch.setTextColor(ContextCompat.getColor(binding.root.context, R.color.white))
            }


        }
    }
}
