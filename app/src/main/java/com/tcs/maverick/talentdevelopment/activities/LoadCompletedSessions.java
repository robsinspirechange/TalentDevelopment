package com.tcs.maverick.talentdevelopment.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tcs.maverick.talentdevelopment.R;
import com.tcs.maverick.talentdevelopment.beans.CompletedSessionsBean;
import com.tcs.maverick.talentdevelopment.utilities.AppConstants;
import com.tcs.maverick.talentdevelopment.utilities.DividerItemDecorator;
import com.tcs.maverick.talentdevelopment.utilities.HttpManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by abhi on 3/27/2016.
 */
public class LoadCompletedSessions extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private ArrayList<CompletedSessionsBean> data = new ArrayList<>();
    private ArrayList<CompletedSessionsBean> searchData = new ArrayList<>();
    private RecyclerView recyclerView;
    private CustomAdapter customAdapter;
    private Menu menu;
    private TextView messageText;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_layout1);
        getSupportActionBar().setTitle("Completed Sessions");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.list);
        customAdapter = new CustomAdapter(searchData);
        messageText = (TextView) findViewById(R.id.messageText);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeLayout);
        recyclerView.setLayoutManager(new LinearLayoutManager(LoadCompletedSessions.this));
        recyclerView.addItemDecoration(new DividerItemDecorator(LoadCompletedSessions.this, LinearLayoutManager.VERTICAL));
        onRefresh();

        swipeRefreshLayout.setOnRefreshListener(this);


    }

    public void getData() {
        SharedPreferences sharedPreferences = getSharedPreferences("Talent Development", MODE_PRIVATE);
        String employeeId = sharedPreferences.getString("emp_id", "");
        String url = AppConstants.URL + "trainer_completed_sessions.php?emp_id=" + employeeId;

        HttpManager httpManager = new HttpManager(LoadCompletedSessions.this, new HttpManager.ServiceResponse() {

            @Override
            public void onServiceResponse(String serviceResponse) {
                if (serviceResponse != null && HttpManager.getStatusCode() == 200) {
                    //Log.d("My Tag", serviceResponse);
                    try {
                        data = new ArrayList<>();
                        searchData = new ArrayList<>();
                        JSONArray jsonArray = new JSONArray(serviceResponse);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                            JSONObject jsonObject1 = jsonObject.getJSONObject("data");

                            String sessionId = jsonObject1.getString("session_id");
                            String sessionName = jsonObject1.getString("session_name");
                            String strDate = jsonObject1.getString("end_date");

                            Date date = new SimpleDateFormat("yyyy-mm-dd").parse(strDate);
                            String startDate = new SimpleDateFormat("dd/mm/yyyy").format(date);
                            //Log.d("My Tag:", startDate);

                            CompletedSessionsBean completedSessionsBean = new CompletedSessionsBean(sessionId, sessionName, startDate);
                            data.add(completedSessionsBean);
                            searchData.add(completedSessionsBean);

                        }

                        if (data.size() > 0) {
                            customAdapter = new CustomAdapter(searchData);
                            recyclerView.setVisibility(View.VISIBLE);
                            recyclerView.setAdapter(customAdapter);
                            swipeRefreshLayout.setRefreshing(false);
                        } else {
                            recyclerView.setVisibility(View.GONE);
                            messageText.setVisibility(View.VISIBLE);
                            messageText.setText("There are no completed sessions.");
                        }


                    } catch (JSONException e) {
                        recyclerView.setVisibility(View.GONE);
                        messageText.setVisibility(View.VISIBLE);
                        messageText.setText("There are no completed sessions.");
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                } else {
                    recyclerView.setVisibility(View.GONE);
                    messageText.setVisibility(View.VISIBLE);
                    messageText.setText("No internet connect, please connect to internet and try again.");
                }
            }
        });
        httpManager.execute(url);
    }


    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        getData();
    }

    private class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
        ArrayList<CompletedSessionsBean> data;
        Context context;
        private ArrayList<ViewHolder> viewH = new ArrayList<>();

        public CustomAdapter(ArrayList<CompletedSessionsBean> data) {
            this.data = data;
            this.context = context;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View v;
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.completed_courses_row, viewGroup, false);
            ViewHolder viewHolder = new ViewHolder(v);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(CustomAdapter.ViewHolder viewHolder, final int i) {
            viewH.add(viewHolder);
            viewHolder.courseTitle.setText(data.get(i).getSessionName());
            viewHolder.completedOn.setText("Completed on: " + data.get(i).getCompletedOn());
            viewHolder.messageText.setVisibility(View.VISIBLE);
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(LoadCompletedSessions.this, CompletedCoursesDetailsActivity.class);
                    intent.putExtra("courseId", data.get(i).getSessionId());
                    intent.putExtra("courseName", data.get(i).getSessionName());
                    startActivity(intent);
                }
            });
        }


        @Override
        public int getItemCount() {
            return data.size();
        }


        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView courseTitle, completedOn, messageText;

            public ViewHolder(View itemView) {
                super(itemView);
                courseTitle = (TextView) itemView.findViewById(R.id.courseTitle);
                completedOn = (TextView) itemView.findViewById(R.id.completedOn);
                messageText = (TextView) itemView.findViewById(R.id.messageText);

            }

        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.courses, menu);
        this.menu = menu;
        MenuItem myActionMenuItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) myActionMenuItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!searchView.isIconified()) {
                    searchView.setIconified(true);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                searchData.clear();
                for (int i = 0; i < data.size(); i++) {
                    if (data.get(i).getSessionName().toLowerCase().contains(s.toLowerCase())) {
                        searchData.add(data.get(i));
                    }
                }
                customAdapter.notifyDataSetChanged();
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            onRefresh();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}