/*
 * This file is a part of Telegram X
 * Copyright © 2014-2022 (tgx-android@pm.me)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * File created on 18/08/2017
 */
package org.thunderdog.challegram.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;

import org.drinkless.td.libcore.telegram.TdApi;
import org.thunderdog.challegram.core.Lang;
import org.thunderdog.challegram.data.DoubleTextWrapper;
import org.thunderdog.challegram.loader.ImageReceiver;
import org.thunderdog.challegram.navigation.TooltipOverlayView;
import org.thunderdog.challegram.telegram.Tdlib;
import org.thunderdog.challegram.theme.Theme;
import org.thunderdog.challegram.tool.Paints;
import org.thunderdog.challegram.tool.Screen;

import me.vkryl.android.util.InvalidateContentProvider;

public class SmallChatView extends BaseView implements AttachDelegate, TooltipOverlayView.LocationProvider, InvalidateContentProvider {
  private final ImageReceiver receiver;

  private DoubleTextWrapper chat;

  public SmallChatView (Context context, Tdlib tdlib) {
    super(context, tdlib);

    int viewHeight = Screen.dp(62f);
    int radius = Screen.dp(50f) / 2;
    this.receiver = new ImageReceiver(this, radius);
    layoutReceiver();
    setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, viewHeight));
  }

  private void layoutReceiver () {
    int viewHeight = Screen.dp(62f);
    int radius = Screen.dp(50f) / 2;
    int left = Screen.dp(11f);
    int right = Screen.dp(11f) + radius * 2;
    if (Lang.rtl()) {
      int viewWidth = getMeasuredWidth();
      this.receiver.setBounds(viewWidth - right, viewHeight / 2 - radius, viewWidth - left, viewHeight / 2 + radius);
    } else {
      this.receiver.setBounds(left, viewHeight / 2 - radius, right, viewHeight / 2 + radius);
    }
  }

  @Override
  public void attach () {
    receiver.attach();
  }

  @Override
  public void detach () {
    receiver.detach();
  }

  @Override
  protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    final int width = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
    if (width > 0 && chat != null) {
      chat.layout(width);
    }
    layoutReceiver();
  }

  public void setChat (DoubleTextWrapper chat) {
    if (this.chat == chat) {
      return;
    }

    if (this.chat != null) {
      this.chat.getViewProvider().detachFromView(this);
    }

    this.chat = chat;
    if (chat != null) {
      setPreviewChatId(null, chat.getChatId(), null);
    } else {
      clearPreviewChat();
    }

    if (chat != null) {
      final int currentWidth = getMeasuredWidth();
      if (currentWidth != 0) {
        chat.layout(currentWidth);
      }
      chat.getViewProvider().attachToView(this);
    }

    requestFile();
    invalidate();
  }

  private void requestFile () {
    receiver.requestFile(chat != null ? chat.getAvatarFile() : null);
  }

  @Override
  public boolean invalidateContent (Object cause) {
    if (this.chat == cause) {
      requestFile();
      return true;
    }
    return false;
  }

  @Override
  public void getTargetBounds (View targetView, Rect outRect) {
    if (chat != null) {
      chat.getTargetBounds(targetView, outRect);
    }
  }

  @Override
  protected void onDraw (Canvas c) {
    if (chat == null) {
      return;
    }

    layoutReceiver();

    if (chat.getAvatarFile() != null) {
      if (receiver.needPlaceholder()) {
        receiver.drawPlaceholderRounded(c, receiver.getRadius());
      }
      receiver.draw(c);
    } else if (chat.getAvatarPlaceholder() != null) {
      chat.getAvatarPlaceholder().draw(c, receiver.centerX(), receiver.centerY());
    }

    chat.draw(this, receiver, c);

    if (checkboxVisible) {
      c.save();
      final float lineSize = Screen.dp(2f);
      float cx = getWidth() - Screen.dp(26);
      float cy = getHeight() / 2f;
      float r2 = Screen.dp(10f);
      c.drawCircle(cx, cy, r2, Paints.fillingPaint(Theme.radioFillingColor()));

      float x1 = cx - Screen.dp(2);
      float y1 = cy + Screen.dp(5f);
      float w2 = Screen.dp(11);// * checkedFactor.getFloatValue();
      float h1 = Screen.dp(5.5f);// * checkedFactor.getFloatValue();

      c.rotate(-45f, x1, y1);
      c.drawRect(x1, y1 - h1, x1 + lineSize, y1, Paints.fillingPaint(Theme.radioCheckColor()));
      c.drawRect(x1, y1 - lineSize, x1 + w2, y1, Paints.fillingPaint(Theme.radioCheckColor()));
      c.restore();
    }
  }

  private boolean checkboxVisible = false;

  public TdApi.MessageSender getSenderId () {
    if (chat == null) return null;
    return chat.getSenderId();
  }

  public void setCheckboxIconVisible (boolean checkboxVisible) {
    if (chat == null) return;
    this.checkboxVisible = checkboxVisible;
    chat.setAdminSignVisible(!checkboxVisible, true);
    invalidate();
  }
}
