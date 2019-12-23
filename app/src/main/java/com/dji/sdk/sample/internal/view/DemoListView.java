package com.dji.sdk.sample.internal.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import com.dji.sdk.sample.R;
import com.dji.sdk.sample.demo.CustomViews.FlightCustomExtendedView;
import com.dji.sdk.sample.demo.CustomViews.FlightCustomFinalView;
import com.dji.sdk.sample.demo.CustomViews.FlightCustomView;
import com.dji.sdk.sample.internal.controller.DJISampleApplication;
import com.dji.sdk.sample.internal.controller.ExpandableListAdapter;
import com.dji.sdk.sample.internal.controller.MainActivity;
import com.dji.sdk.sample.internal.model.GroupHeader;
import com.dji.sdk.sample.internal.model.GroupItem;
import com.squareup.otto.Subscribe;

import static com.dji.sdk.sample.internal.model.ListItem.ListBuilder;

/**
 * This view is in charge of showing all the demos in a list.
 */

public class DemoListView extends FrameLayout {

    private ExpandableListAdapter listAdapter;
    private ExpandableListView expandableListView;

    public DemoListView(Context context) {
        this(context, null, 0);
    }

    public DemoListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DemoListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.demo_list_view, this);

        // Build model for ListView
        ListBuilder builder = new ListBuilder();

        builder.addGroup(R.string.component_listview_custom_creations,
                         false,
                new GroupItem(R.string.custom_creations_drone_control, FlightCustomView.class),
                new GroupItem(R.string.custom_creations_drone_control_extended, FlightCustomExtendedView.class),
                new GroupItem(R.string.custom_creations_drone_control_final, FlightCustomFinalView.class));

        // Set-up ExpandableListView
        expandableListView = (ExpandableListView) view.findViewById(R.id.expandable_list);
        listAdapter = new ExpandableListAdapter(context, builder.build());
        expandableListView.setAdapter(listAdapter);
        DJISampleApplication.getEventBus().register(this);
        expandAllGroupIfNeeded();
    }

    @Subscribe
    public void onSearchQueryEvent(MainActivity.SearchQueryEvent event) {
        listAdapter.filterData(event.getQuery());
        expandAllGroup();
    }

    /**
     * Expands all the group that has children
     */
    private void expandAllGroup() {
        int count = listAdapter.getGroupCount();
        for (int i = 0; i < count; i++) {
            expandableListView.expandGroup(i);
        }
    }

    /**
     * Expands all the group that has children
     */
    private void expandAllGroupIfNeeded() {
        int count = listAdapter.getGroupCount();
        for (int i = 0; i < count; i++) {
            if (listAdapter.getGroup(i) instanceof GroupHeader
                && ((GroupHeader) listAdapter.getGroup(i)).shouldCollapseByDefault()) {
                expandableListView.collapseGroup(i);
            } else {
                expandableListView.expandGroup(i);
            }
        }
    }
}
