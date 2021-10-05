package sh.brennan.universalvaccinepassport.android.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import sh.brennan.universalvaccinepassport.R
import sh.brennan.universalvaccinepassport.classes.room.Passport

class PassportListAdapter(
    private val dataSet: List<Passport>, val listener: ContentListener
) :
    RecyclerView.Adapter<PassportListAdapter.ViewHolder>() {

    public interface ContentListener {
        fun onItemClicked(item: View, text: String)
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View, private val listener: ContentListener) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.passport_name)

        init {
            // Define click listener for the ViewHolder's View.
            view.setOnClickListener { item ->
                run {
                    listener.onItemClicked(item as View, textView.text.toString())
                }
            }
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.passport_list_item, viewGroup, false)

        return ViewHolder(view, listener)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.textView.text = dataSet[position].nickname
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}