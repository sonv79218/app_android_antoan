package com.example.ebook.view;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ebook.R;
import com.example.ebook.api.ApiClient;
import com.example.ebook.api.ReportApi;
import com.example.ebook.utils.SessionManager;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportActivity extends AppCompatActivity {

    private TextView tvBooks, tvUsers, tvViews, tvComments;
    private PieChart pieChart;
    private LineChart lineChartComments, lineChartNewUsers;
    private String range = "7d";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        tvBooks = findViewById(R.id.tvTotalBooks);
        tvUsers = findViewById(R.id.tvTotalUsers);
        tvViews = findViewById(R.id.tvTotalViews);
        tvComments = findViewById(R.id.tvTotalComments);
        pieChart = findViewById(R.id.pieChart);
        lineChartComments = findViewById(R.id.lineChartComments);
        lineChartNewUsers = findViewById(R.id.lineChartNewUsers);

        String token = "Bearer " + new SessionManager(this).getToken();
        ReportApi reportApi = ApiClient.getClient(this).create(ReportApi.class);

        reportApi.getOverview(token).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> data = (Map<String, Object>) response.body().get("data");

                    tvBooks.setText("Tổng số sách: " + getInt(data.get("totalBooks")));
                    tvUsers.setText("Tổng số người dùng: " + getInt(data.get("totalUsers")));
                    tvComments.setText("Tổng số bình luận: " + getInt(data.get("totalComments")));
                    tvViews.setText("Tổng lượt đọc: " + getInt(data.get("totalViews")));
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(ReportActivity.this, "Lỗi tải thống kê", Toast.LENGTH_SHORT).show();
            }
        });

        Spinner spinnerRange = findViewById(R.id.spinnerRange);
        spinnerRange.setSelection(0); // mặc định 1 tuần

        spinnerRange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] rangeValues = {"7d", "1m", "1y"};
                range = rangeValues[position];

                // Gọi lại API khi thay đổi khoảng thời gian
                String token = "Bearer " + new SessionManager(ReportActivity.this).getToken();
                loadCommentStats(token);     // biểu đồ bình luận
                loadNewUserStats(token);     // biểu đồ người dùng mới
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });


        loadBooksByCategory(token);

        MaterialToolbar toolbar = findViewById(R.id.btnBack);
        toolbar.setNavigationOnClickListener(v -> finish());

    }

    private void loadBooksByCategory(String token) {
        ReportApi api = ApiClient.getClient(this).create(ReportApi.class);
        api.getBooksByCategory(token).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, Object>> data = (List<Map<String, Object>>) response.body().get("data");
                    List<PieEntry> entries = new ArrayList<>();
                    for (Map<String, Object> item : data) {
                        String name = item.get("categoryName") != null
                                ? item.get("categoryName").toString()
                                : "Không rõ";
                        int count = getInt(item.get("count"));
                        entries.add(new PieEntry(count, name));
                        Log.d("PieChartDebug", "name = " + name + ", count = " + count);
                    }


                    PieDataSet dataSet = new PieDataSet(entries, "Thể loại");
                    List<Integer> customColors = Arrays.asList(
                            Color.parseColor("#F44336"), // đỏ
                            Color.parseColor("#E91E63"), // hồng
                            Color.parseColor("#9C27B0"), // tím
                            Color.parseColor("#3F51B5"), // xanh đậm
                            Color.parseColor("#03A9F4"), // xanh dương nhạt
                            Color.parseColor("#4CAF50"), // xanh lá
                            Color.parseColor("#FF9800"), // cam
                            Color.parseColor("#795548"), // nâu
                            Color.parseColor("#607D8B"), // xanh xám
                            Color.parseColor("#FFC107"), // vàng đậm
                            Color.parseColor("#009688")  // xanh ngọc
                            // Thêm nữa nếu bạn có nhiều thể loại hơn
                    );

                    dataSet.setColors(customColors);
                    dataSet.setValueTextSize(14f); // Tăng kích thước số
                    dataSet.setValueTextColor(Color.BLACK); // Màu số
                    dataSet.setValueTypeface(Typeface.DEFAULT_BOLD); // In đậm số

                    PieData pieData = new PieData(dataSet);
                    pieData.setValueFormatter(new ValueFormatter() {
                        @Override
                        public String getPieLabel(float value, PieEntry pieEntry) {
                            String label = pieEntry.getLabel();
                            int percent = Math.round(value);
                            return label + ":" + percent + "%";
                        }
                    });
                    pieChart.setData(pieData);

                    pieChart.setUsePercentValues(true);
                    pieChart.getDescription().setEnabled(false);
                    pieChart.setDrawEntryLabels(false);


                    pieChart.getLegend().setTextSize(14f);
                    pieChart.getLegend().setTextColor(Color.BLACK);
                    pieChart.getLegend().setTypeface(Typeface.DEFAULT_BOLD);

                    pieChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
                    pieChart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
                    pieChart.getLegend().setOrientation(Legend.LegendOrientation.HORIZONTAL);
                    pieChart.getLegend().setDrawInside(false);
                    pieChart.getLegend().setEnabled(false);

                    pieChart.invalidate();

                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(ReportActivity.this, "Lỗi biểu đồ thể loại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCommentStats(String token) {
        ReportApi api = ApiClient.getClient(this).create(ReportApi.class);
        api.getCommentStats(token, range).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Object rawData = response.body().get("data");
                    if (!(rawData instanceof List)) {
                        Log.e("ChartComment", "Dữ liệu không đúng định dạng List: " + rawData);
                        return;
                    }

                    List<Map<String, Object>> data = (List<Map<String, Object>>) rawData;
                    Log.d("ChartComment", "Dữ liệu bình luận nhận được: " + data.toString());

                    if (data.isEmpty()) {
                        Log.w("ChartComment", "Danh sách bình luận rỗng");
                        return;
                    }

                    Map<String, Integer> dateCountMap = new TreeMap<>((d1, d2) -> {
                        String[] p1 = d1.split("-");
                        String[] p2 = d2.split("-");
                        String ymd1 = p1[2] + p1[1] + p1[0]; // yyyyMMdd
                        String ymd2 = p2[2] + p2[1] + p2[0];
                        return ymd1.compareTo(ymd2);
                    });

                    for (Map<String, Object> item : data) {
                        Object dateObj = item.get("date");
                        if (dateObj == null) continue;

                        String date = dateObj.toString();
                        int current = dateCountMap.getOrDefault(date, 0);
                        dateCountMap.put(date, current + 1);
                    }

                    List<Entry> entries = new ArrayList<>();
                    List<String> labels = new ArrayList<>();
                    int index = 0;

                    for (Map.Entry<String, Integer> entry : dateCountMap.entrySet()) {
                        entries.add(new Entry(index, entry.getValue()));
                        labels.add(entry.getKey());
                        index++;
                    }

                    LineDataSet dataSet = new LineDataSet(entries, "");
                    dataSet.setColor(Color.BLUE);
                    dataSet.setCircleColor(Color.BLUE);
                    dataSet.setLineWidth(2f);

                    dataSet.setValueTextSize(14f);
                    dataSet.setValueTextColor(Color.BLACK);
                    dataSet.setValueTypeface(Typeface.DEFAULT_BOLD);
                    dataSet.setCircleRadius(5f);
                    dataSet.setCircleHoleRadius(2f);

                    lineChartComments.setData(new LineData(dataSet));

                    XAxis xAxis = lineChartComments.getXAxis();
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setGranularity(1f);
                    xAxis.setLabelRotationAngle(0f);

                    xAxis.setTextSize(14f);
                    xAxis.setTextColor(Color.BLACK);
                    xAxis.setTypeface(Typeface.DEFAULT_BOLD);

                    xAxis.setValueFormatter(new ValueFormatter() {
                        @Override
                        public String getFormattedValue(float value) {
                            int index = (int) value;
                            if (index >= 0 && index < labels.size()) {
                                String fullDate = labels.get(index); // vd: "30-06-2025"
                                String[] parts = fullDate.split("-");
                                if (parts.length == 3) {
                                    return parts[0] + "-" + parts[1];
                                }
                                return fullDate;
                            }
                            return "";
                        }
                    });


                    lineChartComments.getAxisLeft().setAxisMinimum(0f);
                    lineChartComments.getAxisLeft().setTextSize(14f);
                    lineChartComments.getAxisLeft().setTextColor(Color.BLACK);
                    lineChartComments.getAxisLeft().setTypeface(Typeface.DEFAULT_BOLD);
                    lineChartComments.getLegend().setTextSize(14f); // tăng kích thước
                    lineChartComments.getLegend().setTextColor(Color.BLACK); // đổi màu
                    lineChartComments.getLegend().setTypeface(Typeface.DEFAULT_BOLD); // in đậm
                    lineChartComments.getAxisRight().setEnabled(false);
                    lineChartComments.getLegend().setEnabled(false);
                    lineChartComments.setExtraOffsets(0f, 0f, 0f, 24f);
                    lineChartComments.getDescription().setEnabled(false);
                    lineChartComments.invalidate();
                } else {
                    Log.e("ChartComment", "Lỗi response: code=" + response.code() + " body=" + response.body());
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e("ChartComment", "API call failed: " + t.getMessage(), t);
                Toast.makeText(ReportActivity.this, "Lỗi biểu đồ bình luận", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadNewUserStats(String token) {
        ReportApi api = ApiClient.getClient(this).create(ReportApi.class);
        api.getNewUsersStats(token, range).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, Object>> data = (List<Map<String, Object>>) response.body().get("data");

                    // Gom tất cả users từ các group khác nhau (nếu có)
                    Map<String, Integer> dayCountMap = new TreeMap<>((d1, d2) -> {
                        String[] p1 = d1.split("-");
                        String[] p2 = d2.split("-");
                        return (p1[1] + p1[0]).compareTo(p2[1] + p2[0]); // sort theo MMdd
                    });

                    for (Map<String, Object> item : data) {
                        List<Map<String, Object>> users = (List<Map<String, Object>>) item.get("users");
                        if (users == null) continue;

                        for (Map<String, Object> user : users) {
                            Object createdAtObj = user.get("created_at");
                            if (createdAtObj == null) continue;

                            String createdAt = createdAtObj.toString(); // "2025-07-02T05:31:08.790Z"
                            String[] parts = createdAt.split("T")[0].split("-"); // ["2025", "07", "02"]
                            if (parts.length != 3) continue;

                            String label = parts[2] + "-" + parts[1]; // dd-MM
                            int count = dayCountMap.getOrDefault(label, 0);
                            dayCountMap.put(label, count + 1);
                        }
                    }

                    List<Entry> entries = new ArrayList<>();
                    List<String> labels = new ArrayList<>();
                    int i = 0;
                    for (Map.Entry<String, Integer> entry : dayCountMap.entrySet()) {
                        entries.add(new Entry(i, entry.getValue()));
                        labels.add(entry.getKey());
                        i++;
                    }

                    LineDataSet dataSet = new LineDataSet(entries, "");
                    dataSet.setColor(Color.MAGENTA);
                    dataSet.setCircleColor(Color.MAGENTA);
                    dataSet.setLineWidth(2f);
                    dataSet.setValueTextSize(14f);
                    dataSet.setValueTextColor(Color.BLACK);
                    dataSet.setValueTypeface(Typeface.DEFAULT_BOLD);
                    dataSet.setCircleRadius(5f);
                    dataSet.setCircleHoleRadius(2f);

                    lineChartNewUsers.setData(new LineData(dataSet));

                    XAxis xAxis = lineChartNewUsers.getXAxis();
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setGranularity(1f);
                    xAxis.setLabelRotationAngle(0f);
                    xAxis.setTextSize(14f);
                    xAxis.setTextColor(Color.BLACK);
                    xAxis.setTypeface(Typeface.DEFAULT_BOLD);

                    xAxis.setValueFormatter(new ValueFormatter() {
                        @Override
                        public String getFormattedValue(float value) {
                            int index = (int) value;
                            if (index >= 0 && index < labels.size()) {
                                return labels.get(index);
                            }
                            return "";
                        }
                    });

                    lineChartNewUsers.getAxisLeft().setGranularity(1f);
                    lineChartNewUsers.getAxisLeft().setAxisMinimum(0f);
                    lineChartNewUsers.getAxisLeft().setTextSize(14f);
                    lineChartNewUsers.getAxisLeft().setTextColor(Color.BLACK);
                    lineChartNewUsers.getAxisLeft().setTypeface(Typeface.DEFAULT_BOLD);
                    lineChartNewUsers.getLegend().setEnabled(false);
                    lineChartNewUsers.getAxisRight().setEnabled(false);
                    lineChartNewUsers.getDescription().setEnabled(false);
                    lineChartNewUsers.setExtraOffsets(0f, 0f, 0f, 24f);
                    lineChartNewUsers.invalidate();
                } else {
                    Toast.makeText(ReportActivity.this, "Lỗi dữ liệu người dùng mới", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(ReportActivity.this, "Lỗi biểu đồ người dùng mới", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private int getInt(Object obj) {
        if (obj instanceof Double) return ((Double) obj).intValue();
        if (obj instanceof Integer) return (Integer) obj;
        return 0;
    }
}
