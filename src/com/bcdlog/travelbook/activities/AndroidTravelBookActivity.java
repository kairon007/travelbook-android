package com.bcdlog.travelbook.activities;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Date;

import org.json.JSONObject;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

import com.bcdlog.travelbook.FileStorage;
import com.bcdlog.travelbook.R;
import com.bcdlog.travelbook.TBPreferences;
import com.bcdlog.travelbook.Utils;
import com.bcdlog.travelbook.activities.followers.FollowersListFragment;
import com.bcdlog.travelbook.activities.items.ItemsListFragment;
import com.bcdlog.travelbook.activities.login.LoginActivity;
import com.bcdlog.travelbook.activities.settings.SettingsActivity;
import com.bcdlog.travelbook.database.FollowersDbAdapter;
import com.bcdlog.travelbook.database.Item;
import com.bcdlog.travelbook.database.ItemsDbAdapter;
import com.bcdlog.travelbook.network.Requester;
import com.bcdlog.travelbook.network.Synchronizer;

/**
 * Startup class
 * 
 * @author bcdlog
 * 
 */
public class AndroidTravelBookActivity extends Activity {

    private static final int PICK_CONTACT = 0;
    private static final int PICK_PHOTO = 1;
    private static final int CAPTURE_IMAGE = 2;
    private static final int CAPTURE_VIDEO = 3;
    private static final int CAPTURE_AUDIO = 4;
    public static final String DEFAULT_EXTENSION = ".mp4";

    public static AndroidTravelBookActivity instance;
    private static FollowersDbAdapter followersDbAdapter;
    private static ItemsDbAdapter itemsDbAdapter;
    public static JSONObject jsonObjectFromLogin;

    private TBListFragment currentFragment;
    private Item creatingItem;
    private Menu menu;
    private Tab itemsTab;
    private Tab followersTab;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	instance = this;
	followersDbAdapter = new FollowersDbAdapter(this);
	itemsDbAdapter = new ItemsDbAdapter(this);

	ActionBar actionBar = getActionBar();
	actionBar.setHomeButtonEnabled(true);
	actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

	itemsTab = actionBar.newTab();
	itemsTab.setText(R.string.items_tab_name);
	itemsTab.setTabListener(new TabListener<ItemsListFragment>(this,
		ItemsListFragment.class));
	actionBar.addTab(itemsTab);

	followersTab = actionBar.newTab();
	followersTab.setText(R.string.followers_tab_name);
	followersTab.setTabListener(new TabListener<FollowersListFragment>(
		this, FollowersListFragment.class));
	actionBar.addTab(followersTab);

    }

    public void startupRefresh() {
	if (getIntent().getAction().equals(LoginActivity.REFRESH)) {
	    refresh();
	} else if (getIntent().getAction().equals(LoginActivity.LOGIN)) {
	    Synchronizer synchronizer = new Synchronizer();
	    synchronizer.synchronize(jsonObjectFromLogin);
	}
    }

    public FollowersDbAdapter getFollowersDbAdapter() {
	return followersDbAdapter;
    }

    public ItemsDbAdapter getItemsDbAdapter() {
	return itemsDbAdapter;
    }

    private class TabListener<T extends Fragment> implements
	    ActionBar.TabListener {

	private Fragment mFragment;
	private final Activity mActivity;
	private final String mTag;
	private final Class<T> mClass;

	/**
	 * Constructor used each time a new tab is created.
	 * 
	 * @param activity
	 *            The host Activity, used to instantiate the fragment
	 * @param clz
	 *            The fragment's Class, used to instantiate the fragment
	 */
	public TabListener(Activity activity, Class<T> clz) {
	    mActivity = activity;
	    mTag = clz.getSimpleName();
	    mClass = clz;
	}

	/* The following are each of the ActionBar.TabListener callbacks */

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
	    // Check if the fragment is already initialized
	    if (mFragment == null) {
		// If not, instantiate and add it to the activity
		mFragment = Fragment.instantiate(mActivity, mClass.getName());
		ft.add(android.R.id.content, mFragment, mTag);
	    } else {
		// If it exists, simply attach it in order to show it
		// ft.attach(mFragment);
		swap(true);
	    }
	    currentFragment = (TBListFragment) mFragment;

	    if (menu != null) {
		onCreateOptionsMenu(menu);
	    }
	}

	private void swap(boolean visible) {
	    FragmentTransaction ft = getFragmentManager().beginTransaction();
	    // ft.setCustomAnimations(android.R.animator.fade_in,
	    // android.R.animator.fade_out);
	    if (visible) {
		ft.show(mFragment);
	    } else {
		ft.hide(mFragment);
	    }
	    ft.commit();
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	    if (mFragment != null) {
		// Detach the fragment, because another one is being attached
		// ft.detach(mFragment);
		swap(false);
	    }
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	    // User selected the already selected tab. Usually do nothing.
	}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	this.menu = menu;
	menu.clear();
	MenuInflater inflater = getMenuInflater();
	if (currentFragment != null
		&& currentFragment instanceof FollowersListFragment) {
	    inflater.inflate(R.menu.add_contact, menu);
	} else {
	    inflater.inflate(R.menu.add, menu);
	    inflater.inflate(R.menu.refresh, menu);
	}
	inflater.inflate(R.menu.settings, menu);
	return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
	try {
	    Intent intent = null;
	    switch (menuItem.getItemId()) {
	    case android.R.id.home:
		itemsTab.select();
		return true;
	    case R.id.refresh:
		refresh();
		return true;
	    case R.id.add:
		if (currentFragment instanceof FollowersListFragment) {
		    intent = new Intent(Intent.ACTION_PICK,
			    ContactsContract.Contacts.CONTENT_URI);
		    try {
			startActivityForResult(intent, PICK_CONTACT);
		    } catch (Throwable t) {
			final AlertDialog.Builder alert = new AlertDialog.Builder(
				this);

			alert.setTitle(R.string.add_contact);
			alert.setMessage(R.string.enter_email);

			// Set an EditText view to get user input
			final EditText editText = (EditText) getLayoutInflater()
				.inflate(R.layout.add_contact, null);
			alert.setView(editText);

			alert.setPositiveButton(R.string.add,
				new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog,
					    int whichButton) {
					String email = editText.getText()
						.toString();
					if (email != null && !email.isEmpty()) {
					    FollowersListFragment.instance
						    .saveFollower(email, email);
					}
				    }
				});

			alert.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog,
					    int whichButton) {
					// Canceled.
				    }
				});

			alert.show();
		    }
		    return true;
		} else {
		    return false;
		}
	    case R.id.pick_photo:
		intent = new Intent();
		intent.setType("*/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(
			Intent.createChooser(intent,
				getResources().getString(R.string.pick_photo)),
			PICK_PHOTO);
		return true;
	    case R.id.take_photo:
		creatingItem = itemsDbAdapter.createItem("image/jpeg", ".jpg",
			new Date());
		try {
		    intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		    FileStorage.getInstance().getItemFileOutputStream(
			    creatingItem, false);
		    intent.putExtra(MediaStore.EXTRA_OUTPUT,
			    Uri.fromFile(creatingItem.getFile()));
		    startActivityForResult(intent, CAPTURE_IMAGE);
		} catch (Throwable t) {
		    Utils.logError(t);
		    FileStorage.getInstance().deleteFiles(creatingItem);
		}
		return true;
	    case R.id.take_video:
		creatingItem = itemsDbAdapter.createItem("video/mp4",
			DEFAULT_EXTENSION, new Date());
		try {
		    intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
		    FileStorage.getInstance().getItemFileOutputStream(
			    creatingItem, false);
		    intent.putExtra(MediaStore.EXTRA_OUTPUT,
			    Uri.fromFile(creatingItem.getFile()));
		    startActivityForResult(intent, CAPTURE_VIDEO);
		} catch (Throwable t) {
		    Utils.logError(t);
		    FileStorage.getInstance().deleteFiles(creatingItem);
		}
		return true;
	    case R.id.record:
		startActivity(new Intent(this, AudioCaptureActivity.class));
		// startActivity(new Intent(this, AudioRecordActivity.class));
		// startActivityForResult(new Intent(
		// MediaStore.Audio.Media.RECORD_SOUND_ACTION), CAPTURE_AUDIO);

		// Invoke default recorder. Nice interface but cannot record on
		// A710
		// creatingItem = itemsDbAdapter.createItem("audio/mp4", ".mp4",
		// new Date());
		// try {
		// intent = new Intent(
		// MediaStore.Audio.Media.RECORD_SOUND_ACTION);
		// startActivityForResult(intent, CAPTURE_AUDIO);
		// } catch (Throwable t) {
		// Utils.logError(t);
		// }
		return true;
	    case R.id.write_text:
		startActivity(new Intent(this, EditActivity.class));
		return true;
	    case R.id.settings:
		startActivity(new Intent(this, SettingsActivity.class));
		return true;
	    default:
		return super.onOptionsItemSelected(menuItem);
	    }
	} catch (Throwable t) {
	    Utils.logError(t);
	    return false;
	}
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent intent) {
	super.onActivityResult(reqCode, resultCode, intent);

	switch (reqCode) {
	case PICK_CONTACT:
	    if (resultCode == RESULT_OK) {
		if (intent != null) {
		    Uri uri = intent.getData();
		    if (uri != null) {
			FollowersListFragment.instance.addContact(uri);
		    }
		}
	    } else {
		Utils.logError("Failed to pick contact resultCode="
			+ resultCode);
	    }
	    break;
	case PICK_PHOTO:
	    if (resultCode == RESULT_OK) {
		ItemsListFragment.instance.pickMedia(intent.getData());
	    } else {
		Utils.logError("Failed to pick picture resultCode="
			+ resultCode);
	    }
	    break;
	// case SELECT_VIDEO:
	// if (resultCode == RESULT_OK) {
	// ItemsListFragment.instance.pickVideo(intent.getData());
	// } else {
	// Utils.logError("Failed to pick video resultCode=" + resultCode);
	// }
	// break;
	case CAPTURE_IMAGE:
	case CAPTURE_VIDEO:
	    boolean remove = true;
	    try {
		if (resultCode == RESULT_OK) {
		    ItemsListFragment.instance.addMediaItem(creatingItem,
			    intent);
		    remove = false;
		} else {
		    Utils.logError("Failed resultCode=" + resultCode);
		}
	    } finally {
		if (remove) {
		    FileStorage.getInstance().deleteFiles(creatingItem);
		}
	    }
	    break;
	case CAPTURE_AUDIO:
	    if (resultCode == RESULT_OK) {
		InputStream inputStream = null;
		FileOutputStream fileOutputStream = null;
		try {
		    inputStream = getContentResolver().openInputStream(
			    intent.getData());
		    fileOutputStream = FileStorage.getInstance()
			    .getItemFileOutputStream(creatingItem, false);
		    byte buffer[] = new byte[1024];
		    int count;
		    while ((count = inputStream.read(buffer)) != -1) {
			fileOutputStream.write(buffer, 0, count);
		    }
		    ItemsListFragment.instance.addMediaItem(creatingItem,
			    intent);
		} catch (Throwable t) {
		    Utils.logError(t);
		    FileStorage.getInstance().deleteFiles(creatingItem);
		} finally {
		    try {
			if (inputStream != null) {
			    inputStream.close();
			}
			if (fileOutputStream != null) {
			    fileOutputStream.flush();
			    fileOutputStream.close();
			}
		    } catch (Throwable t) {
			Utils.logError(t);
		    }
		}
	    } else if (resultCode == RESULT_CANCELED) {
		FileStorage.getInstance().deleteFiles(creatingItem);
	    } else {
		Utils.logError("Failed to take "
			+ creatingItem.getContentType()
			+ " or video resultCode=" + resultCode);
	    }
	    break;
	}
    }

    public void refresh() {
	if (TBPreferences.getToken() != null && !Requester.isSynchronizing) {
	    ItemsListFragment.instance.showProgressBar();
	    Requester.getItems();
	}
    }

    @Override
    protected void onDestroy() {
	super.onDestroy();
	if (itemsDbAdapter != null) {
	    itemsDbAdapter.close();
	}
	if (followersDbAdapter != null) {
	    followersDbAdapter.close();
	}
	// Remove pending downloads
	Requester.flushDownloadManager();

    }

    public void saveDescription(Item item, String content, Date date) {
	item.setContent(content);
	item.setContentDate(date);
	if (item.getId() == null) {
	    item.setStatus(ItemsDbAdapter.Status.CREATED.toString());
	    getItemsDbAdapter().createItem(item);
	} else {
	    item.setModified();
	    getItemsDbAdapter().updateItem(item);
	}
	Requester.postItem(item);
    }

}