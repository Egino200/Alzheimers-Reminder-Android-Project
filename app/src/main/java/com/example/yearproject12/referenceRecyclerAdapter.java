package com.example.yearproject12;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class referenceRecyclerAdapter extends RecyclerView.Adapter<referenceRecyclerAdapter.MyViewHolder> {
Context context;
ArrayList<TestReminder> reminderList;
ArrayList<String> parentIDs;

    public referenceRecyclerAdapter(Context context, ArrayList<TestReminder> reminderList){
        this.context = context;
        this.reminderList = reminderList;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recycler_view, parent, false);
        return new MyViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
    //assign values to rows
        holder.title.setText(reminderList.get(position).getTitle());
        holder.time.setText(reminderList.get(position).getTime());
        holder.button.setText("Remove");
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                 FirebaseDatabase database = FirebaseDatabase.getInstance();
                 DatabaseReference reminder = database.getReference().child("References");

                Query query = reminder.orderByChild("title");

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Iterate over the ordered children
                        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                            String childOrder = childSnapshot.child("title").getValue(String.class);


                            if (Objects.equals(childOrder, reminderList.get(position).getTitle())) {
                                // Remove the child node
                                childSnapshot.getRef().removeValue();
                                break;  // Exit the loop after removing the child node
                            }
                        }
                        notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        //amount of items
        return reminderList.size();
    }

    public static  class MyViewHolder extends RecyclerView.ViewHolder{

        TextView title;
        TextView time;
        Button button;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.titleView);
            time = itemView.findViewById(R.id.timeView);
            button = itemView.findViewById(R.id.button);

        }
    }
}
