package wangdaye.com.geometricweather.view.dialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoDialogFragment;
import wangdaye.com.geometricweather.view.adapter.TranslatorAdapter;

/**
 * Translator dialog.
 * */

public class TranslatorDialog extends GeoDialogFragment
        implements View.OnClickListener {
    // widget
    private CoordinatorLayout container;
    private RecyclerView recyclerView;

    /** <br> life cycle. */

    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_translator, null, false);
        this.initWidget(view);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        return builder.create();
    }

    @Override
    public View getSnackbarContainer() {
        return container;
    }

    /** <br> UI. */

    private void initWidget(View view) {
        this.container = (CoordinatorLayout) view.findViewById(R.id.dialog_translator_container);

        this.recyclerView = (RecyclerView) view.findViewById(R.id.dialog_translator_recyclerView);
        recyclerView.setAdapter(new TranslatorAdapter(getActivity()));
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        view.findViewById(R.id.dialog_translator_header).setOnClickListener(this);
        view.findViewById(R.id.dialog_translator_enterBtn).setOnClickListener(this);
    }

    /** <br> interface. */

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.dialog_translator_header:
                recyclerView.smoothScrollToPosition(0);
                break;

            case R.id.dialog_translator_enterBtn:
                dismiss();
                break;
        }
    }
}
