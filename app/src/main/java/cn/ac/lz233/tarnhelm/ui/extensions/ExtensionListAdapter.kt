package cn.ac.lz233.tarnhelm.ui.extensions

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import cn.ac.lz233.tarnhelm.R
import cn.ac.lz233.tarnhelm.extension.ExtensionManager
import cn.ac.lz233.tarnhelm.extension.ExtensionRecord
import cn.ac.lz233.tarnhelm.util.LogUtil
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch

class ExtensionListAdapter(
    private val extensionList: MutableList<ExtensionRecord>
) : RecyclerView.Adapter<ExtensionListAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val extensionEnableSwitch: MaterialSwitch = view.findViewById(R.id.extensionEnableSwitch)
        val nameContentTextView: AppCompatTextView = view.findViewById(R.id.nameContentTextView)
        val descriptionContentTextView: AppCompatTextView = view.findViewById(R.id.descriptionContentTextView)
        val regexesContentTextView: AppCompatTextView = view.findViewById(R.id.regexesContentTextView)
        val authorContentTextView: AppCompatTextView = view.findViewById(R.id.authorContentTextView)
        val configureButton: MaterialButton = view.findViewById(R.id.configureButton)
        val deleteButton: AppCompatImageButton = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_extension, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ext = extensionList[position]
        holder.nameContentTextView.text = ext.name
        holder.descriptionContentTextView.text = ext.description
        holder.regexesContentTextView.text = ext.regexes.joinToString("\n")
        holder.authorContentTextView.text = ext.author

        holder.extensionEnableSwitch.setOnCheckedChangeListener(null)
        holder.extensionEnableSwitch.isChecked = ext.enabled
        holder.extensionEnableSwitch.setOnCheckedChangeListener { _, isChecked ->
            runCatching {
                if (isChecked) ExtensionManager.enableExtension(ext)
                else ExtensionManager.disableExtension(ext)
                extensionList[position] = ext.copy(enabled = isChecked)
            }.onFailure { e -> LogUtil.e(e) }
        }

        if (ext.hasConfigurationPanel) {
            holder.configureButton.visibility = View.VISIBLE
            holder.configureButton.setOnClickListener {
                val activity = holder.itemView.context as? Activity ?: return@setOnClickListener
                runCatching {
                    ExtensionManager.startExtensionConfigurationPanel(ext, activity)
                }.onFailure { e -> LogUtil.e(e) }
            }
        } else {
            holder.configureButton.visibility = View.GONE
        }

        holder.deleteButton.setOnClickListener {
            runCatching {
                ExtensionManager.uninstallExtension(ext)
                val idx = holder.adapterPosition
                if (idx != RecyclerView.NO_POSITION) {
                    extensionList.removeAt(idx)
                    notifyItemRemoved(idx)
                    notifyItemRangeChanged(idx, extensionList.size - idx)
                }
            }.onFailure { e -> LogUtil.e(e) }
        }
    }

    override fun getItemCount() = extensionList.size
}
