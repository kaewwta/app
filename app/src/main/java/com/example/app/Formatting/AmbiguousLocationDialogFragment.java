package com.example.app;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.LocationsRecyclerAdapter;
import com.example.app.MainActivity;
import com.example.app.R;
import com.example.app.Weather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;

import com.example.app.Weather;
import com.example.app.Formatting;
import com.example.app.UnitConvertor;

import static com.example.app.TimeUtils.isDayTime;

public class AmbiguousLocationDialogFragment extends DialogFragment implements LocationsRecyclerAdapter.ItemClickListener {

    private LocationsRecyclerAdapter recyclerAdapter;
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate( R.layout.fragment_dialog_ambiguous_location, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Formatting formatting = new Formatting(getActivity());
        final Bundle bundle = getArguments();
        final Toolbar toolbar = view.findViewById(R.id.dialogToolbar);
        final RecyclerView recyclerView = view.findViewById(R.id.locationsRecyclerView);
        final LinearLayout linearLayout = view.findViewById(R.id.locationsLinearLayout);

        toolbar.setTitle(getString(R.string.location_search_heading));

        toolbar.setNavigationIcon(R.drawable.ic_close_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                close();
            }
        });

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());



        try {
            final JSONArray cityListArray = new JSONArray(bundle.getString("cityList"));
            final ArrayList< Weather > weatherArrayList = new ArrayList<>();
            recyclerAdapter = new LocationsRecyclerAdapter(view.getContext().getApplicationContext(),
                    weatherArrayList);

            recyclerAdapter.setClickListener(AmbiguousLocationDialogFragment.this);

            for (int i = 0; i < cityListArray.length(); i++) {
                final JSONObject cityObject = cityListArray.getJSONObject(i);
                final JSONObject weatherObject = cityObject.getJSONArray("weather").getJSONObject(0);
                final JSONObject mainObject = cityObject.getJSONObject("main");
                final JSONObject coordObject = cityObject.getJSONObject("coord");
                final JSONObject sysObject = cityObject.getJSONObject("sys");

                final Calendar calendar = Calendar.getInstance();
                final String dateMsString = cityObject.getString("dt") + "000";
                final String city = cityObject.getString("name");
                final String country = sysObject.getString("country");
                final String cityId = cityObject.getString("id");
                final String description = weatherObject.getString("description");
                final String weatherId = weatherObject.getString("id");
                final float temperature = UnitConvertor.convertTemperature( Float.parseFloat(mainObject.getString("temp")), sharedPreferences);
                final double lat = coordObject.getDouble("lat");
                final double lon = coordObject.getDouble("lon");

                calendar.setTimeInMillis( Long.parseLong(dateMsString));

                Weather weather = new Weather();
                weather.setCity(city);
                weather.setCountry(country);
                weather.setId(cityId);
                weather.setDescription(description.substring(0, 1).toUpperCase() + description.substring(1));
                weather.setLat(lat);
                weather.setLon(lon);
                weather.setIcon(formatting.setWeatherIcon( Integer.parseInt(weatherId), isDayTime(weather, calendar)));

                if (sharedPreferences.getBoolean("displayDecimalZeroes", false)) {
                    weather.setTemperature(new DecimalFormat("0.0").format(temperature) + " " + sharedPreferences.getString("unit", "°C"));
                } else {
                    weather.setTemperature(new DecimalFormat("#.#").format(temperature) + " " + sharedPreferences.getString("unit", "°C"));
                }

                weatherArrayList.add(weather);
            }

            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.setAdapter(recyclerAdapter);


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("ApplySharedPref")
    @Override
    public void onItemClickListener(View view, int position) {
        final Weather weather = recyclerAdapter.getItem(position);
        final Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags( Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        final Bundle bundle = new Bundle();

        sharedPreferences.edit().putString("cityId", weather.getId()).commit();
        bundle.putBoolean("shouldRefresh", true);
        intent.putExtras(bundle);

        startActivity(intent);
        close();
    }

    private int getTheme(String themePref) {
        switch (themePref) {
            case "dark":
                return R.style.AppTheme_NoActionBar_Dark;
            case "black":
                return R.style.AppTheme_NoActionBar_Black;
            case "classic":
                return R.style.AppTheme_NoActionBar_Classic;
            case "classicdark":
                return R.style.AppTheme_NoActionBar_Classic_Dark;
            case "classicblack":
                return R.style.AppTheme_NoActionBar_Classic_Black;
            default:
                return R.style.AppTheme_NoActionBar;
        }
    }

    private void close() {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            activity.getSupportFragmentManager().popBackStack();
        }
    }
}
