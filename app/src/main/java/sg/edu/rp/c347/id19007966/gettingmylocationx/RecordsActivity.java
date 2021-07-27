package sg.edu.rp.c347.id19007966.gettingmylocationx;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class RecordsActivity extends AppCompatActivity {

    ListView recordsListView;
    Button refreshButton;
    TextView numberOfRecordsTextView;

    ArrayList<String> coordinates;
    ArrayAdapter<String> recordsAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);

        recordsListView = findViewById(R.id.recordsListView);
        refreshButton = findViewById(R.id.refreshButton);
        numberOfRecordsTextView = findViewById(R.id.noOfRecordsTextView);

        coordinates = new ArrayList<>();
        recordsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, coordinates);
        recordsListView.setAdapter(recordsAdapter);

        updateList();

        refreshButton.setOnClickListener(view -> {
            updateList();
        });
    }

    private void updateList() {
        String folderLocation = getFilesDir().getAbsolutePath() + "/LocationLogs";
        File locationLog = new File(folderLocation, "log.txt");

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
    }
}