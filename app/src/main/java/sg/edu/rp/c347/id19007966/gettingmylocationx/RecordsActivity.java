package sg.edu.rp.c347.id19007966.gettingmylocationx;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

public class RecordsActivity extends AppCompatActivity {

    ListView recordsListView;
    Button refreshButton, favouriteButton;
    TextView numberOfRecordsTextView;

    ArrayList<String> coordinates;
    ArrayAdapter<String> recordsAdapter;

    Boolean showFavourite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);

        recordsListView = findViewById(R.id.recordsListView);
        refreshButton = findViewById(R.id.refreshButton);
        favouriteButton = findViewById(R.id.favouritesButton);
        numberOfRecordsTextView = findViewById(R.id.noOfRecordsTextView);

        coordinates = new ArrayList<>();
        recordsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, coordinates);
        recordsListView.setAdapter(recordsAdapter);

        updateList();

        refreshButton.setOnClickListener(view -> {
            updateList();
        });

        recordsListView.setOnItemClickListener((adapterView, view, idx, l) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Add this location to your favorite list?")
                    .setPositiveButton("Yes", (dialog, id) -> {
                        addToFav(coordinates.get(idx));
                    })
                    .setNegativeButton("No", null);
            if (!showFavourite) {
                builder.show();
            }
        });

        favouriteButton.setOnClickListener(view -> {
            showFavourite = !showFavourite;
            favouriteButton.setText(showFavourite ? "All Records" : "Favourite");
            updateList();
        });
    }

    private void addToFav(String coordinate) {
        String folderLocation = getFilesDir().getAbsolutePath() + "/LocationLogs";
        File favouriteLog = new File(folderLocation, "favourites.txt");
        try {
            FileWriter writer = new FileWriter(favouriteLog, true);
            writer.write(coordinate + "\n");
            writer.flush();
            writer.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void updateList() {
        String folderLocation = getFilesDir().getAbsolutePath() + "/LocationLogs";

        File locationLog;

        if (showFavourite) {
            locationLog = new File(folderLocation, "favourites.txt");
        }
        else {
            locationLog = new File(folderLocation, "log.txt");
        }

        if (locationLog.exists()) {
            String data = "";
            try {
                coordinates.clear();
                FileReader reader = new FileReader(locationLog);
                BufferedReader bufferedReader = new BufferedReader(reader);

                String line = bufferedReader.readLine();
                while (line != null) {
                    coordinates.add(line);
                    line = bufferedReader.readLine();
                }
                bufferedReader.close();
                reader.close();
            }
            catch (Exception e) {
                Toast.makeText(this, "Fail to read file", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            recordsAdapter.notifyDataSetChanged();
            numberOfRecordsTextView.setText("" + coordinates.size());
        }
        else {
            coordinates.clear();
            recordsAdapter.notifyDataSetChanged();
            numberOfRecordsTextView.setText("0");
        }
    }
}