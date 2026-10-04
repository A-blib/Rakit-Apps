package com.aris.templateapp.ui.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;

import com.aris.templateapp.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Grafik area sederhana untuk tren download per hari (alur-provider.md bagian 3.7): garis warna foreground,
 * area bergradasi memudar ke bawah, grid horizontal tipis, label sumbu Geist Mono, tanpa bayangan.
 * Ketuk/geser di grafik untuk melihat tanggal & jumlah pada titik terdekat.
 * <p>
 * Dibuat sendiri (bukan library) agar gaya monokromnya persis mengikuti token desain dan tidak menambah dependency.
 * Semua warna diambil dari resource, jadi otomatis mengikuti mode terang/gelap.
 */
public class AreaChartView extends View {

    /** Satu titik: label sumbu X (mis. "Sen"), label tooltip (mis. "Sen, 4 Okt"), dan nilai. */
    public static final class Point {
        final String axisLabel;
        final String detailLabel;
        final long value;

        public Point(String axisLabel, String detailLabel, long value) {
            this.axisLabel = axisLabel;
            this.detailLabel = detailLabel;
            this.value = value;
        }

        public String getAxisLabel() {
            return axisLabel;
        }

        public String getDetailLabel() {
            return detailLabel;
        }

        public long getValue() {
            return value;
        }
    }

    private static final int GRID_LINES = 4;

    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tooltipBackground = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tooltipBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tooltipText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path linePath = new Path();
    private final Path fillPath = new Path();
    private final RectF tooltipRect = new RectF();

    private final float labelGap;
    private final float pointRadius;
    private final float tooltipPadding;
    private final float cornerRadius;

    private List<Point> points = new ArrayList<>();
    /** Tampilkan label X setiap n titik (30 hari terlalu rapat jika semua diberi label). */
    private int labelEvery = 1;
    private int selected = -1;
    private final int touchSlop;
    private float downX;
    private float downY;

    public AreaChartView(@NonNull Context context) {
        this(context, null);
    }

    public AreaChartView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        int foreground = ContextCompat.getColor(context, R.color.color_foreground);
        int border = ContextCompat.getColor(context, R.color.color_border);
        int muted = ContextCompat.getColor(context, R.color.color_muted);
        Typeface mono = ResourcesCompat.getFont(context, R.font.geist_mono_regular);
        float density = getResources().getDisplayMetrics().density;

        linePaint.setColor(foreground);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(2 * density);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        linePaint.setStrokeCap(Paint.Cap.ROUND);

        fillPaint.setStyle(Paint.Style.FILL);

        gridPaint.setColor(border);
        gridPaint.setStrokeWidth(getResources().getDimension(R.dimen.border_width));

        labelPaint.setColor(muted);
        labelPaint.setTypeface(mono);
        labelPaint.setTextSize(getResources().getDimension(R.dimen.chart_label_text));

        pointPaint.setColor(foreground);

        tooltipBackground.setColor(ContextCompat.getColor(context, R.color.color_surface));
        tooltipBorder.setColor(ContextCompat.getColor(context, R.color.color_border_strong));
        tooltipBorder.setStyle(Paint.Style.STROKE);
        tooltipBorder.setStrokeWidth(getResources().getDimension(R.dimen.border_width));
        tooltipText.setColor(foreground);
        tooltipText.setTypeface(mono);
        tooltipText.setTextSize(getResources().getDimension(R.dimen.chart_label_text));

        labelGap = getResources().getDimension(R.dimen.space_2);
        pointRadius = 3 * density;
        tooltipPadding = getResources().getDimension(R.dimen.space_2);
        cornerRadius = getResources().getDimension(R.dimen.radius_small);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public void setPoints(List<Point> points, int labelEvery) {
        this.points = points == null ? new ArrayList<>() : new ArrayList<>(points);
        this.labelEvery = Math.max(1, labelEvery);
        this.selected = -1;
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (points.isEmpty()) {
            return;
        }
        long maxValue = niceMax(maxValue());
        float labelHeight = labelPaint.getTextSize();
        float yLabelWidth = labelPaint.measureText(String.valueOf(maxValue));
        float left = getPaddingLeft() + yLabelWidth + labelGap;
        float right = getWidth() - getPaddingRight() - pointRadius;
        float top = getPaddingTop() + labelHeight;
        float bottom = getHeight() - getPaddingBottom() - labelHeight - labelGap;

        drawGrid(canvas, maxValue, left, right, top, bottom);
        drawArea(canvas, maxValue, left, right, top, bottom);
        drawXLabels(canvas, left, right);
        if (selected >= 0 && selected < points.size()) {
            drawSelection(canvas, maxValue, left, right, top, bottom);
        }
    }

    private void drawGrid(Canvas canvas, long maxValue, float left, float right, float top, float bottom) {
        for (int i = 0; i <= GRID_LINES; i++) {
            float y = bottom - (bottom - top) * i / GRID_LINES;
            canvas.drawLine(left, y, right, y, gridPaint);
            String label = String.valueOf(maxValue * i / GRID_LINES);
            canvas.drawText(label, getPaddingLeft(), y + labelPaint.getTextSize() / 3, labelPaint);
        }
    }

    private void drawArea(Canvas canvas, long maxValue, float left, float right, float top, float bottom) {
        linePath.reset();
        fillPath.reset();
        for (int i = 0; i < points.size(); i++) {
            float x = xOf(i, left, right);
            float y = yOf(points.get(i).value, maxValue, top, bottom);
            if (i == 0) {
                linePath.moveTo(x, y);
                fillPath.moveTo(x, bottom);
            }
            linePath.lineTo(x, y);
            fillPath.lineTo(x, y);
        }
        fillPath.lineTo(xOf(points.size() - 1, left, right), bottom);
        fillPath.close();

        // Gradasi warna foreground: cukup terlihat di atas, hilang di dasar grafik.
        int color = linePaint.getColor();
        fillPaint.setShader(new LinearGradient(0, top, 0, bottom,
                ColorUtils.setAlphaComponent(color, 70), ColorUtils.setAlphaComponent(color, 0), Shader.TileMode.CLAMP));
        canvas.drawPath(fillPath, fillPaint);
        canvas.drawPath(linePath, linePaint);
    }

    private void drawXLabels(Canvas canvas, float left, float right) {
        float y = getHeight() - getPaddingBottom();
        int last = points.size() - 1;
        // Label terakhir (hari ini) selalu ditampilkan agar ujung kanan grafik jelas; label lain yang akan
        // menabraknya dilewati (mis. "30/9" tepat sebelum "4/10").
        float lastX = labelX(last, left, right);
        for (int i = 0; i < last; i++) {
            if (i % labelEvery != 0) {
                continue;
            }
            float x = labelX(i, left, right);
            if (x + labelPaint.measureText(points.get(i).axisLabel) + labelGap > lastX) {
                continue;
            }
            canvas.drawText(points.get(i).axisLabel, x, y, labelPaint);
        }
        canvas.drawText(points.get(last).axisLabel, lastX, y, labelPaint);
    }

    /** Posisi kiri label sumbu X: di tengah titiknya, tetapi tidak keluar dari tepi grafik. */
    private float labelX(int index, float left, float right) {
        float width = labelPaint.measureText(points.get(index).axisLabel);
        return Math.max(left, Math.min(xOf(index, left, right) - width / 2, getWidth() - getPaddingRight() - width));
    }

    private void drawSelection(Canvas canvas, long maxValue, float left, float right, float top, float bottom) {
        Point point = points.get(selected);
        float x = xOf(selected, left, right);
        float y = yOf(point.value, maxValue, top, bottom);
        canvas.drawLine(x, top, x, bottom, tooltipBorder);
        canvas.drawCircle(x, y, pointRadius * 1.5f, pointPaint);

        String text = getContext().getString(R.string.chart_tooltip, point.detailLabel, point.value);
        float width = tooltipText.measureText(text) + tooltipPadding * 2;
        float height = tooltipText.getTextSize() + tooltipPadding * 2;
        float boxLeft = Math.max(left, Math.min(x - width / 2, right - width));
        tooltipRect.set(boxLeft, top - tooltipText.getTextSize(), boxLeft + width, top - tooltipText.getTextSize() + height);
        canvas.drawRoundRect(tooltipRect, cornerRadius, cornerRadius, tooltipBackground);
        canvas.drawRoundRect(tooltipRect, cornerRadius, cornerRadius, tooltipBorder);
        canvas.drawText(text, tooltipRect.left + tooltipPadding,
                tooltipRect.bottom - tooltipPadding - tooltipText.descent() / 2, tooltipText);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (points.isEmpty()) {
            return false;
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                // Sejak jari menempel, geseran dipegang grafik dulu: ViewPager2 (geser tab) dan ScrollView
                // tidak boleh mengambil alih sebelum arah geseran jelas.
                getParent().requestDisallowInterceptTouchEvent(true);
                select(event.getX());
                return true;
            case MotionEvent.ACTION_MOVE:
                float dx = Math.abs(event.getX() - downX);
                float dy = Math.abs(event.getY() - downY);
                if (dy > touchSlop && dy > dx) {
                    // Geseran tegak: lepaskan ke ScrollView agar layar tetap bisa di-scroll dari atas grafik.
                    getParent().requestDisallowInterceptTouchEvent(false);
                    return false;
                }
                // Geseran mendatar = memilih titik (bukan pindah tab).
                select(event.getX());
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    private void select(float x) {
        float left = getPaddingLeft() + labelPaint.measureText(String.valueOf(niceMax(maxValue()))) + labelGap;
        float right = getWidth() - getPaddingRight() - pointRadius;
        float step = points.size() == 1 ? 1 : (right - left) / (points.size() - 1);
        selected = Math.max(0, Math.min(points.size() - 1, Math.round((x - left) / step)));
        invalidate();
    }

    private float xOf(int index, float left, float right) {
        return points.size() == 1 ? (left + right) / 2 : left + (right - left) * index / (points.size() - 1);
    }

    private static float yOf(long value, long maxValue, float top, float bottom) {
        return bottom - (bottom - top) * value / (float) maxValue;
    }

    private long maxValue() {
        long max = 0;
        for (Point point : points) {
            max = Math.max(max, point.value);
        }
        return max;
    }

    /** Nilai atas sumbu Y yang "bulat" dan habis dibagi jumlah garis grid (minimal 4), agar label grid berupa bilangan bulat. */
    static long niceMax(long max) {
        if (max <= GRID_LINES) {
            return GRID_LINES;
        }
        long step = (long) Math.ceil(max / (double) GRID_LINES);
        long magnitude = (long) Math.pow(10, Math.floor(Math.log10(step)));
        long[] nice = {1, 2, 5, 10};
        for (long n : nice) {
            if (n * magnitude >= step) {
                return n * magnitude * GRID_LINES;
            }
        }
        return step * GRID_LINES;
    }
}
