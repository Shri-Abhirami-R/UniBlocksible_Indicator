package com.scanner.app.tracking

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.scanner.app.tracking.data.model.ManufacturerDetail

class ManufacturerDetailAdapter(manufacturerDetail: ArrayList<ManufacturerDetail>) : RecyclerView.Adapter<ManufacturerDetailAdapter.ManufacturerItemViewHolder>() {

    var manufacturerDetail: ArrayList<ManufacturerDetail> = manufacturerDetail

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ManufacturerItemViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_manufacturer, parent, false)
        return ManufacturerItemViewHolder(itemView)
    }

    class ManufacturerItemViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {

        val context = view.context!!
        var name : TextView = view.findViewById(R.id.name_tv)
        var date: TextView = view.findViewById(R.id.date_tv)
        //var location: TextView = view.findViewById(R.id.location_tv)

    }

    override fun onBindViewHolder(holder: ManufacturerItemViewHolder, position: Int) {
        holder.name.text = manufacturerDetail.get(position).manufacturerName
        holder.date.text = manufacturerDetail.get(position).createdTime

    }

    override fun getItemCount(): Int {
       return manufacturerDetail.size
    }
}