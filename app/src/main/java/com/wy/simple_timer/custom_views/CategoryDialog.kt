import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.widget.EditText
import com.github.gzuliyujiang.colorpicker.ColorPicker

class CategoryDialog(private val context: Activity) {
    interface CategoryDialogListener {
        fun onConfirmEdit(newName: String, newColor: String)
        fun onPickColor(newColor: String)
    }

    private lateinit var editText: EditText
    private var selectedColor: String = "#4CAF50"
    private lateinit var dialog: AlertDialog
    private var listener: CategoryDialogListener? = null

    fun setListener(listener: CategoryDialogListener) {
        this.listener = listener
    }

    fun show(initialName: String, initialColor: String) {
        selectedColor = initialColor
        editText = EditText(context).apply {
            setText(initialName)
            setTextColor(Color.parseColor(selectedColor))
            setHintTextColor(Color.parseColor(selectedColor))
            // 单行限制
            inputType = android.text.InputType.TYPE_CLASS_TEXT
            maxLines = 1
            setSingleLine(true)
            imeOptions = android.view.inputmethod.EditorInfo.IME_ACTION_DONE
        }

        dialog = AlertDialog.Builder(context)
            .setTitle("编辑分类")
            .setView(editText)
            .setPositiveButton("确定") { _, _ ->
                listener?.onConfirmEdit(editText.text.toString(), selectedColor)
            }
            .setNegativeButton("取消", null)
            .setNeutralButton("选择颜色") { _, _ ->
                showColorPicker()
            }
            .create()
            
        dialog.show()
    }

    private fun showColorPicker() {
        ColorPicker(context).apply {
            setInitColor(Color.parseColor(selectedColor))
            setOnColorPickListener { color ->
                selectedColor = String.format("#%06X", 0xFFFFFF and color)
                editText.setTextColor(Color.parseColor(selectedColor))
                editText.setHintTextColor(Color.parseColor(selectedColor))
                listener?.onPickColor(selectedColor)
                dialog.show()  // 重新显示主对话框
            }
            show()
        }
    }
}
