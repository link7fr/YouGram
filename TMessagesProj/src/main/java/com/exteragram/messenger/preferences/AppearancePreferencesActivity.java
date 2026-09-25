/*

 This is the source code of exteraGram for Android.

 We do not and cannot prevent the use of our code,
 but be respectful and credit the original author.

 Copyright @immat0x1, 2023

*/
package com.exteragram.messenger.preferences;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.exteragram.messenger.ExteraConfig;
import com.exteragram.messenger.preferences.components.AvatarCornersPreviewCell;
import com.exteragram.messenger.preferences.components.ChatListPreviewCell;
import com.exteragram.messenger.preferences.components.FabShapeCell;
import com.exteragram.messenger.preferences.components.SolarIconsPreview;
import com.exteragram.messenger.utils.AppUtils;
import com.exteragram.messenger.utils.ChatUtils;
import com.exteragram.messenger.utils.LocaleUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.SharedConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;

public class AppearancePreferencesActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    private ListAdapter listAdapter;
    private RecyclerListView listView;

    private int rowCount;
    private int previewRow;
    private int solarIconsRow;
    private int materialYouRow;
    private int materialYouPrivacyRow;
    private int avatarCornersRow;
    private int fabShapeRow;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        updateRows();
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.didSetNewTheme);
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didSetNewTheme);
    }

    private void updateRows() {
        rowCount = 0;
        previewRow = rowCount++;
        solarIconsRow = rowCount++;
        
        if (Build.VERSION.SDK_INT >= 31) {
            materialYouRow = rowCount++;
            materialYouPrivacyRow = rowCount++;
        } else {
            materialYouRow = -1;
            materialYouPrivacyRow = -1;
        }
        
        avatarCornersRow = rowCount++;
        fabShapeRow = rowCount++;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(org.telegram.messenger.R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle("Интерфейс YouGram");

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        listAdapter = new ListAdapter(context);

        fragmentView = new RecyclerListView(context);
        listView = (RecyclerListView) fragmentView;
        listView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(context, androidx.recyclerview.widget.LinearLayoutManager.VERTICAL, false));
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener((view, position) -> {
            if (position == materialYouRow && view instanceof TextCheckCell) {
                TextCheckCell cell = (TextCheckCell) view;
                boolean isEnabled = MessagesController.getGlobalMainSettings().getBoolean("material_you_enabled", false);
                
                MessagesController.getGlobalMainSettings().edit().putBoolean("material_you_enabled", !isEnabled).commit();
                cell.setChecked(!isEnabled);
                
                if (!isEnabled) {
                    applyMaterialYouColors(context);
                } else {
                    Theme.initializeThemeColors();
                }
                
                Theme.reloadAllResources(getParentActivity());
                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.didSetNewTheme);
            }
        });

        return fragmentView;
    }

    private void applyMaterialYouColors(Context context) {
        if (Build.VERSION.SDK_INT < 31) return;

        try {
            int systemAccentColor = context.getColor(android.R.color.system_accent1_500);
            int systemBackgroundColor = context.getColor(android.R.color.system_neutral1_100);
            int systemLightAccent = context.getColor(android.R.color.system_accent1_100);
            
            boolean isDarkTheme = (context.getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK) 
                    == android.content.res.Configuration.UI_MODE_NIGHT_YES;
            
            if (isDarkTheme) {
                systemBackgroundColor = context.getColor(android.R.color.system_neutral1_900);
                systemLightAccent = context.getColor(android.R.color.system_accent1_700);
            }

            Theme.setColor("windowBackgroundWhite", systemBackgroundColor);
            Theme.setColor("windowBackgroundGray", isDarkTheme ? Color.parseColor("#131418") : Color.parseColor("#f1f5f9"));
            Theme.setColor("actionBarDefault", systemAccentColor);
            Theme.setColor("actionBarDefaultIcon", isDarkTheme ? Color.WHITE : Color.BLACK);
            Theme.setColor("actionBarDefaultTitle", isDarkTheme ? Color.WHITE : Color.BLACK);
            Theme.setColor("chat_wallpaper", systemBackgroundColor);
            Theme.setColor("chat_outBubble", systemLightAccent);
            Theme.setColor("chat_inBubble", isDarkTheme ? Color.parseColor("#232429") : Color.WHITE);
            Theme.setColor("chat_outBubbleSelected", systemAccentColor);
            Theme.setColor("switchTrackChecked", systemLightAccent);
            Theme.setColor("switch2TrackChecked", systemLightAccent);
            Theme.setColor("checkboxCheck", systemAccentColor);
            Theme.setColor("chats_actionBackground", systemAccentColor);
            
            Theme.saveCurrentThemeSelf();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.didSetNewTheme) {
            if (listView != null) {
                listView.invalidateViews();
            }
        }
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {
        private Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int position = holder.getAdapterPosition();
            return position == materialYouRow;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 1:
                    view = new ChatListPreviewCell(mContext);
                    break;
                case 2:
                    view = new SolarIconsPreview(mContext);
                    break;
                case 3:
                    view = new AvatarCornersPreviewCell(mContext);
                    break;
                case 4:
                    view = new FabShapeCell(mContext);
                    break;
                case 5:
                    view = new TextCheckCell(mContext);
                    break;
                case 6:
                default:
                    view = new TextInfoPrivacyCell(mContext);
                    break;
            }
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            int viewType = getItemViewType(position);
            switch (viewType) {
                case 5:
                    if (position == materialYouRow) {
                        TextCheckCell checkCell = (TextCheckCell) holder.itemView;
                        boolean isEnabled = MessagesController.getGlobalMainSettings().getBoolean("material_you_enabled", false);
                        checkCell.setTextAndCheck("Динамические цвета Material You", isEnabled, false);
                    }
                    break;
                case 6:
                    if (position == materialYouPrivacyRow) {
                        TextInfoPrivacyCell privacyCell = (TextInfoPrivacyCell) holder.itemView;
                        privacyCell.setText("Автоматически адаптирует палитру YouGram под обои твоего рабочего стола (Работает на Android 12+)");
                    }
                    break;
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == previewRow) return 1;
            if (position == solarIconsRow) return 2;
            if (position == avatarCornersRow) return 3;
            if (position == fabShapeRow) return 4;
            if (position == materialYouRow) return 5;
            if (position == materialYouPrivacyRow) return 6;
            return 6;
        }
    }
}

