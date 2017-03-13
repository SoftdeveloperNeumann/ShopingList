package com.example.softdeveloper.shopinglist;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by softdeveloper on 28.02.2017.
 */
public class MainActivity extends AppCompatActivity {
    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    private ShoppingMemoDataSource dataSource;
    private ListView shoppingsMemoListView;
    public void onCreate(Bundle savedInstanceStat){
        super.onCreate(savedInstanceStat);
        setContentView(R.layout.activity_main);

        dataSource = new ShoppingMemoDataSource(this);

        initializeShoppingMemosListView();
        activateAddButton();
        initializeContextualActionBar();

    }

    public void onResume(){
        super.onResume();
        Log.d(LOG_TAG, "Die Datenquelle wird geöffnet");
        dataSource.open();
        showAllListEntries();
    }
    public void onPause(){
        super.onPause();
        Log.d(LOG_TAG, "Die Datasource wird geschlossen");
        dataSource.close();
    }

    private void initializeShoppingMemosListView(){
        List<ShoppingMemo> emptyListFORInitializing= new ArrayList<>();
        shoppingsMemoListView = (ListView)findViewById(R.id.listview_shopping_memos);
        ArrayAdapter<ShoppingMemo> shoppingMemoArrayAdapter = new ArrayAdapter<ShoppingMemo>(
                this,android.R.layout.simple_list_item_multiple_choice,emptyListFORInitializing){

           public View getView(int position, View convertView, ViewGroup parent){
               View view = super.getView(position,convertView,parent);
               TextView textView = (TextView)view;
               ShoppingMemo memo = (ShoppingMemo)shoppingsMemoListView.getItemAtPosition(position);
               if(memo.isChecked()){
                   textView.setPaintFlags(textView.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);
                   textView.setTextColor(Color.rgb(175,175,175));
               }else{
                   textView.setPaintFlags(textView.getPaintFlags()& (~Paint.STRIKE_THRU_TEXT_FLAG));
                   textView.setTextColor(Color.DKGRAY);
               }
               return view;
           }
        };
        shoppingsMemoListView.setAdapter(shoppingMemoArrayAdapter);
        shoppingsMemoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ShoppingMemo memo = (ShoppingMemo)parent.getItemAtPosition(position);
                ShoppingMemo updatedShoppingMemo = dataSource.updateShoppingMemo(memo.getId(),
                        memo.getProduct(),memo.getQuantity(),!memo.isChecked());
                showAllListEntries();
            }
        });

    }
    private void activateAddButton(){
        Button buttonAddProduct = (Button) findViewById(R.id.button_add_product);
        final EditText editTextQuantity = (EditText) findViewById(R.id.editText_quantity);
        final EditText editTextProduct = (EditText) findViewById(R.id.editText_product);

        buttonAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quantityString = editTextQuantity.getText().toString();
                String product = editTextProduct.getText().toString();

                if(TextUtils.isEmpty(quantityString)){
                    editTextQuantity.setError(getString(R.string.editText_errorMessage));
                    return;
                }
                if(TextUtils.isEmpty(product)){
                    editTextProduct.setError(getString(R.string.editText_errorMessage));
                    return;
                }
                int quantity = Integer.parseInt(quantityString);
                editTextQuantity.setText("");
                editTextProduct.setText("");

                dataSource.createShopingMemo(product,quantity);

                InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(
                        INPUT_METHOD_SERVICE);
                if(getCurrentFocus()!=null){
                    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
                }

                showAllListEntries();

            }
        });
    }

    private void initializeContextualActionBar(){
        final ListView shoppingMemosListView = (ListView)findViewById(R.id.listview_shopping_memos);
        shoppingMemosListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        shoppingMemosListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            int selCount = 0;
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                if(checked){
                    selCount++;
                }else{
                    selCount--;
                }
                String cabTitel = selCount + " " + getString(R.string.cab_checked_string);
                mode.setTitle(cabTitel);
                mode.invalidate();
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                getMenuInflater().inflate(R.menu.menu_contextual_action_bar,menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                MenuItem item = menu.findItem(R.id.cab_change);
                if(selCount==1){
                    item.setVisible(true);
                }else{
                    item.setVisible(false);
                }
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                getCurrentFocus().clearFocus();
                SparseBooleanArray touchedShoppingMemosPosition =
                        shoppingMemosListView.getCheckedItemPositions();
                switch(item.getItemId()){
                    case R.id.cab_delete:

                        for(int i =0;i<touchedShoppingMemosPosition.size();i++){
                            boolean isChecked = touchedShoppingMemosPosition.valueAt(i);
                            if(isChecked){
                                int positionInListView=touchedShoppingMemosPosition.keyAt(i);
                                ShoppingMemo shoppingMemo = (ShoppingMemo)shoppingMemosListView.
                                        getItemAtPosition(positionInListView);
                                dataSource.deleteShoppingMemo(shoppingMemo);
                            }
                        }
                        showAllListEntries();
                        mode.finish();
                        break;
                    case R.id.cab_change:

                        for(int i =0;i<touchedShoppingMemosPosition.size();i++) {
                            boolean isChecked = touchedShoppingMemosPosition.valueAt(i);
                            if (isChecked) {
                                int positionInListView = touchedShoppingMemosPosition.keyAt(i);
                                ShoppingMemo shoppingMemo = (ShoppingMemo) shoppingMemosListView.
                                        getItemAtPosition(positionInListView);
                                AlertDialog editShoppingMemoDialog = createEditShoppingMemoDialog(shoppingMemo);
                                editShoppingMemoDialog.show();
                            }
                        }
                        mode.finish();
                        break;

                    default:
                        return false;
                }
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                selCount =0;
            }
        });
    }
    private AlertDialog createEditShoppingMemoDialog(final ShoppingMemo shoppingMemo){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogsView = inflater.inflate(R.layout.dialog_edit_shopping_memo,null);
        final EditText editTextNewQuantity = (EditText) dialogsView.findViewById(R.id.editText_new_quantity);
        final EditText editTextNewProduct = (EditText) dialogsView.findViewById(R.id.editText_new_product);

        editTextNewQuantity.setText(String.valueOf(shoppingMemo.getQuantity()));
        editTextNewProduct.setText(shoppingMemo.getProduct());

        builder.setView(dialogsView)
                .setTitle(R.string.dialog_titel)
                .setPositiveButton(R.string.dialog_button_positiv, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String quantityString = editTextNewQuantity.getText().toString();
                        String product = editTextNewProduct.getText().toString();
                        if(TextUtils.isEmpty(quantityString)|| TextUtils.isEmpty(product)){
                            Log.e(LOG_TAG,"Eintrag ohne Inhalt");
                            return;
                        }
                        int quantity = Integer.parseInt(quantityString);
                        ShoppingMemo updateShoppingMemo = dataSource.updateShoppingMemo(shoppingMemo
                                .getId(),product,quantity,shoppingMemo.isChecked());
                        showAllListEntries();
                        dialog.dismiss();
                    }
                }).setNegativeButton(R.string.dialog_button_negativ, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        return builder.create();
    }
    public void showAllListEntries(){
        List<ShoppingMemo> shoppingMemoList = dataSource.getAllShoppingMemos();

        ArrayAdapter<ShoppingMemo> shoppingMemoArrayAdapter = (ArrayAdapter<ShoppingMemo>)
                shoppingsMemoListView.getAdapter();

        shoppingMemoArrayAdapter.clear();
        shoppingMemoArrayAdapter.addAll(shoppingMemoList);
        shoppingMemoArrayAdapter.notifyDataSetChanged();
    }



    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if(id==R.id.action_settings){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
