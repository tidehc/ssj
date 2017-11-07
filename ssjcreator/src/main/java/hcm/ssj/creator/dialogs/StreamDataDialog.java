/*
 * StreamDataDialog.java
 * Copyright (c) 2017
 * Authors: Ionut Damian, Michael Dietz, Frank Gaibler, Daniel Langerenken, Simon Flutura,
 * Vitalijs Krumins, Antonio Grieco
 * *****************************************************
 * This file is part of the Social Signal Interpretation for Java (SSJ) framework
 * developed at the Lab for Human Centered Multimedia of the University of Augsburg.
 *
 * SSJ has been inspired by the SSI (http://openssi.net) framework. SSJ is not a
 * one-to-one port of SSI to Java, it is an approximation. Nor does SSJ pretend
 * to offer SSI's comprehensive functionality and performance (this is java after all).
 * Nevertheless, SSJ borrows a lot of programming patterns from SSI.
 *
 * This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this library; if not, see <http://www.gnu.org/licenses/>.
 */

package hcm.ssj.creator.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import java.io.File;

import hcm.ssj.creator.R;
import hcm.ssj.creator.util.StreamData;

/**
 * Dialog for stream file selection.
 */
public class StreamDataDialog extends DialogFragment
{
	/**
	 * Interface to notify listeners when new stream data directory is selected.
	 */
	public interface OnStreamDataSelectedListener
	{
		/**
		 * Pass stream files to the listener.
		 * @param files Selected stream file.
		 */
		void onStreamDataSelected(File[] files);
	}

	private static final int CHECKED_ITEM_INDEX = 0;

	private OnStreamDataSelectedListener listener;

	private StreamData streamData = new StreamData();
	private File selectedDir = streamData.selectDir(CHECKED_ITEM_INDEX);

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		builder.setPositiveButton(R.string.str_ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i)
			{
				listener.onStreamDataSelected(streamData.getStreamFiles(selectedDir));
			}
		});

		builder.setNegativeButton(R.string.str_cancel, null);

		builder.setSingleChoiceItems(streamData.getDirNames(), CHECKED_ITEM_INDEX,
									 new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				selectedDir = streamData.selectDir(which);
			}
		});

		return builder.create();
	}

	@Override
	public void onAttach(Context context)
	{
		super.onAttach(context);
		try
		{
			listener = (OnStreamDataSelectedListener) context;
		}
		catch (ClassCastException e)
		{
			throw new ClassCastException(context.toString() + " must implement OnStreamDataSelectedListener");
		}
	}
}
