package com.example.mqtt_giga;
import android.app.Dialog;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class SoundPickerDialogFragment extends DialogFragment {

  private static final int 		TYPE_PRESET = 0			;
  private static final int 		TYPE_SYSTEM = 1			;
  private static final long 	PLAY_DURATION_MS = 5000	;

  private SoundAdapter 				adapter				;
  private Ringtone 					currentRingtone		;
  private SoundItem 				selectedItem		;
  private final Handler 			playHandler = new Handler(Looper.getMainLooper());
  private OnSoundSelectedListener 	listener			;
  private ProgressBar             	progressBar			;
  private View                    	contentView			;

  public interface OnSoundSelectedListener {
	void onSoundSelected(Uri soundUri, String soundName)	;}
  public void setOnSoundSelectedListener(OnSoundSelectedListener _listener) { listener = _listener	;}
  @NonNull  @Override
  public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
	AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

	View rootView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_sound_picker, null);

	progressBar = rootView.findViewById(R.id.progress_bar);
	contentView = rootView.findViewById(R.id.content_view);
	// Показываем индикатор загрузки и скрываем контент
	showLoading(true);

	RecyclerView recyclerView = contentView.findViewById(R.id.recycler_view)	;
	Button btnPlay 			  = contentView.findViewById(R.id.btn_play)			;
	Button btnStop 			  = contentView.findViewById(R.id.btn_stop)			;
	Button btnSelect 		  = contentView.findViewById(R.id.btn_select)		;

	// Setup RecyclerView
	recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
	recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
	adapter = new SoundAdapter();
	recyclerView.setAdapter(adapter);

	// Load sounds
	loadSounds();

	// Button listeners
	btnPlay.setBackgroundResource(R.drawable.btn_device_bg)	;
	btnPlay.setBackgroundTintList(null)						; // очищаем стандартную заливку
	btnPlay.setTextColor(Color.BLACK)						;
	btnStop.setBackgroundResource(R.drawable.btn_device_bg)	;
	btnStop.setBackgroundTintList(null)						; // очищаем стандартную заливку
	btnStop.setTextColor(Color.BLACK)						;
	btnSelect.setBackgroundResource(R.drawable.btn_device_bg)	;
	btnSelect.setBackgroundTintList(null)					; // очищаем стандартную заливку
	btnSelect.setTextColor(Color.BLACK)						;

	btnPlay.setOnClickListener(v -> playSelectedSound());
	btnStop.setOnClickListener(v -> stopPlaying());
	btnSelect.setOnClickListener(v -> {
	  if (selectedItem != null && listener != null) {
		Uri uri = getSoundUri(selectedItem);
		listener.onSoundSelected(uri, selectedItem.name);
		dismiss();
	  }
	});

	builder.setView(rootView)
		   .setTitle("Выберите звук уведомления")
		   .setNegativeButton("Отмена", (dialog, which) -> dismiss());

	return builder.create();
  }
  private void showLoading(boolean isLoading) {
	progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
	contentView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
  }

  private void loadSounds() {
	new Thread(() -> {
	  // Имитируем загрузку для демонстрации
	  try {
		Thread.sleep(1000);
	  } catch (InterruptedException e) {
		e.printStackTrace();
	  }
	  List<SoundItem> items = new ArrayList<>();
	  // Preset sounds
	  items.add(new SoundItem("Звук 1", R.raw.horn1		, TYPE_PRESET));
	  items.add(new SoundItem("Звук 2", R.raw.horn20	, TYPE_PRESET));
	  items.add(new SoundItem("Звук 3", R.raw.horn23	, TYPE_PRESET));
	  items.add(new SoundItem("Звук 4", R.raw.msg_error	, TYPE_PRESET));
	  items.add(new SoundItem("Звук 5", R.raw.musicbox2	, TYPE_PRESET));
	  items.add(new SoundItem("Звук 6", R.raw.musicbox3	, TYPE_PRESET));
	  // System sounds
	  RingtoneManager manager = new RingtoneManager(requireContext());
	  manager.setType(RingtoneManager.TYPE_NOTIFICATION);

	  try {
		for (int i = 0; i < manager.getCursor().getCount(); i++) {
		  Uri uri = manager.getRingtoneUri(i);
		  String title = manager.getRingtone(i).getTitle(requireContext());
		  items.add(new SoundItem(title, uri, TYPE_SYSTEM));
		}
	  } catch (Exception e) {
		e.printStackTrace();
	  }

	  // Правильное обновление UI через Handler
	  new Handler(Looper.getMainLooper()).post(() -> {
		if (isAdded()) { // Проверяем, что фрагмент еще прикреплен
		  adapter.setItems(items);
		  showLoading(false);
		}
	  });
	}).start();
  }

  private void playSelectedSound() {
	stopPlaying();
	if (selectedItem == null) return;

	try {
	  Uri soundUri = getSoundUri(selectedItem);
	  currentRingtone = RingtoneManager.getRingtone(requireContext(), soundUri);
	  if (currentRingtone != null) {
		currentRingtone.play();
		playHandler.postDelayed(this::stopPlaying, PLAY_DURATION_MS);
	  }
	} catch (Exception e) {
	  e.printStackTrace();
	}
  }

  private void stopPlaying() {
	playHandler.removeCallbacksAndMessages(null);
	if (currentRingtone != null && currentRingtone.isPlaying()) {
	  currentRingtone.stop();
	}
	currentRingtone = null;
  }

  private Uri getSoundUri(SoundItem item) {
	return item.type == TYPE_PRESET ?
		   Uri.parse("android.resource://" + requireContext().getPackageName() + "/" + item.resId) :
		   item.uri;
  }

  @Override
  public void onDestroyView() {
	stopPlaying();
	playHandler.removeCallbacksAndMessages(null);
	super.onDestroyView();
  }

  @Override
  public void onDetach() {
	super.onDetach();
	listener = null;
  }

  private class SoundAdapter extends RecyclerView.Adapter<SoundAdapter.SoundViewHolder> {
	private List<SoundItem> items = new ArrayList<>();

	public void setItems(List<SoundItem> items) {
	  this.items = items;
	  notifyDataSetChanged();
	}

	@NonNull
	@Override
	public SoundViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
	  View view = LayoutInflater.from(parent.getContext())
								.inflate(R.layout.item_sound, parent, false);
	  return new SoundViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull SoundViewHolder holder, int position) {
	  SoundItem item = items.get(position);
	  holder.bind(item);
	}

	@Override
	public int getItemCount() {
	  return items.size();
	}

	class SoundViewHolder extends RecyclerView.ViewHolder {
	  private final TextView nameView;
	  private final ImageView iconView;
	  private final View selectionIndicator;

	  SoundViewHolder(View itemView) {
		super(itemView);
		nameView = itemView.findViewById(R.id.sound_name);
		iconView = itemView.findViewById(R.id.sound_icon);
		selectionIndicator = itemView.findViewById(R.id.selection_indicator);
	  }

	  void bind(SoundItem item) {
		nameView.setText(item.name);
		iconView.setImageResource(item.type == TYPE_PRESET ?
				  R.drawable.ic_bell : R.drawable.ic_bell_solid);

		boolean isSelected = selectedItem != null && selectedItem.equals(item);
		selectionIndicator.setVisibility(isSelected ? View.VISIBLE : View.INVISIBLE);

		itemView.setOnClickListener(v -> {
		  selectedItem = item;
		  notifyDataSetChanged();
		});
	  }
	}
  }

  private static class SoundItem {
	final String name;
	final int resId;
	final Uri uri;
	final int type;

	SoundItem(String name, int resId, int type) {
	  this.name = name;
	  this.resId = resId;
	  this.uri = null;
	  this.type = type;
	}

	SoundItem(String name, Uri uri, int type) {
	  this.name = name;
	  this.resId = 0;
	  this.uri = uri;
	  this.type = type;
	}

	@Override
	public boolean equals(Object o) {
	  if (this == o) return true;
	  if (o == null || getClass() != o.getClass()) return false;
	  SoundItem soundItem = (SoundItem) o;
	  if (type != soundItem.type) return false;
	  return type == TYPE_PRESET ?
			 resId == soundItem.resId :
			 uri.equals(soundItem.uri);
	}
  }
}