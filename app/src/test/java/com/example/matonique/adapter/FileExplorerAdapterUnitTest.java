package com.example.matonique.adapter;

import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.List;

import com.example.matonique.model.FileItem;
import com.example.matonique.R;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class FileExplorerAdapterUnitTest {

	@Mock
	FileExplorerAdapter.OnItemClickListener listener;

	private AutoCloseable mocks;

	@Before
	public void setUp() {
		mocks = MockitoAnnotations.openMocks(this);
	}

	@After
	public void tearDown() throws Exception {
		if (mocks != null) mocks.close();
	}

	@Test
	public void click_and_longClick_invoke_listener_methods_using_MockAnnotation() {
		List<FileItem> items = Arrays.asList(
				new FileItem("/path/one", "one.mp3", false),
				new FileItem("/path/dir", "dir", true)
		);

		FileExplorerAdapter adapter = new FileExplorerAdapter(items, listener);

		Context ctx = ApplicationProvider.getApplicationContext();
		FrameLayout parent = new FrameLayout(ctx);

		FileExplorerAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);
		adapter.onBindViewHolder(holder, 0);

		TextView txt = holder.itemView.findViewById(R.id.txt_name);
		assertEquals("one.mp3", txt.getText().toString());

		holder.itemView.performClick();
		Mockito.verify(listener).onItemClick(items.get(0));

		holder.itemView.performLongClick();
		Mockito.verify(listener).onItemLongClick(items.get(0));
	}
}



