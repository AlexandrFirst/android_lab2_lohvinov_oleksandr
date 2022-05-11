package oleksandr.lohvinov.lab2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CustomSpinnerAdapter extends ArrayAdapter<String> {

    private Context m_context;
    private String[] spinnerItemValues;
    private Integer[] spinnerItemIcons;


    public CustomSpinnerAdapter(@NonNull Context context, int resource,
                                String[] spinnerItemValues,
                                Integer[] spinnerItemIcons) {
        super(context, resource);

        this.m_context = context;
        this.spinnerItemValues = spinnerItemValues;
        this.spinnerItemIcons = spinnerItemIcons;
    }

    private View getCustomView(int position, View convertView,
                               ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(m_context);
        View layout = inflater.inflate(R.layout.custom_spinner, parent, false);

        ImageView iconSpinnerItem = layout.findViewById(R.id.spinnerImageIconId);
        TextView textSpinnerItem = layout.findViewById(R.id.spinnerTextId);

        int indexSpinnerItemIcon = position % spinnerItemIcons.length;
        int indexSpinnerItemValue = position % spinnerItemValues.length;

        iconSpinnerItem.setImageResource(spinnerItemIcons[indexSpinnerItemIcon]);
        textSpinnerItem.setText(spinnerItemValues[indexSpinnerItemValue]);

        return layout;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public int getCount() {
        return spinnerItemIcons.length;
    }
}
