package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MuhasabaUiState
import com.example.ui.components.SectionCard
import com.example.ui.theme.AmberSoft
import com.example.ui.theme.AmberText
import com.example.ui.theme.AmberTimer
import com.example.util.DateUtils

@Composable
fun TimerScreen(
  state: MuhasabaUiState,
  onStartTimer: () -> Unit,
  onStopTimer: () -> Unit,
  onDeleteSession: (Int) -> Unit,
) {
  val s = state.strings
  val isRunning = state.activeTimerStartMs != null
  val day = state.currentDay
  val sessions = day.carelessnessSessions
  val totalMs = day.totalCarelessnessMs()

  Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
    SectionCard {
      Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Box(
          modifier =
            Modifier.size(56.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(
                if (state.isDarkTheme) Color(0xFF451A03).copy(alpha = 0.6f) else AmberSoft
              ),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Filled.Schedule,
            contentDescription = s.timerTitle,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(30.dp),
          )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
          text = s.timerTitle,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = s.timerSubtitle,
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(22.dp))
        Text(
          text = DateUtils.formatMsToHHMMSS(state.elapsedTimerMs),
          fontSize = 38.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(24.dp))
        if (!isRunning) {
          Button(
            onClick = onStartTimer,
            colors =
              ButtonDefaults.buttonColors(
                containerColor = AmberTimer,
                contentColor = Color.White,
              ),
            shape = RoundedCornerShape(14.dp),
            modifier =
              Modifier.fillMaxWidth().height(50.dp).testTag("start_timer_button"),
          ) {
            Text(
              text = s.startTimerButton,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
            )
          }
        } else {
          Button(
            onClick = onStopTimer,
            colors =
              ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = Color.White,
              ),
            shape = RoundedCornerShape(14.dp),
            modifier =
              Modifier.fillMaxWidth().height(50.dp).testTag("stop_timer_button"),
          ) {
            Text(
              text = s.stopTimerButton,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
            )
          }
        }
      }
    }

    SectionCard {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = s.timerSessionsHeader,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
          text = String.format(s.timerTotalFormat, DateUtils.formatMsToHHMMSS(totalMs)),
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = if (state.isDarkTheme) MaterialTheme.colorScheme.tertiary else AmberText,
        )
      }

      Spacer(modifier = Modifier.height(12.dp))
      if (sessions.isEmpty()) {
        Text(
          text = s.timerNoSessions,
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          sessions.forEachIndexed { idx, sess ->
            Row(
              modifier =
                Modifier.fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f))
                  .border(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant,
                    RoundedCornerShape(12.dp),
                  )
                  .padding(horizontal = 12.dp, vertical = 6.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text =
                    "${DateUtils.formatTimeHHMM(sess.startMs)} – ${DateUtils.formatTimeHHMM(sess.endMs)}",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                )
                Text(
                  text = "  (${DateUtils.formatMsToHHMMSS(sess.durationMs)})",
                  fontSize = 11.sp,
                  fontFamily = FontFamily.Monospace,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
              IconButton(
                onClick = { onDeleteSession(idx) },
                modifier = Modifier.size(40.dp),
              ) {
                Icon(
                  imageVector = Icons.Filled.DeleteOutline,
                  contentDescription = s.deleteSessionDesc,
                  tint = MaterialTheme.colorScheme.error,
                )
              }
            }
          }
        }
      }
    }
  }
}
