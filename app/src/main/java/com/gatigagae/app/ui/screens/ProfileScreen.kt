package com.gatigagae.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gatigagae.app.data.DogProfile
import com.gatigagae.app.data.FakeData
import com.gatigagae.app.data.Gear
import com.gatigagae.app.ui.components.PrimaryButton
import com.gatigagae.app.ui.components.SecondaryButton
import com.gatigagae.app.ui.components.SubText
import com.gatigagae.app.ui.components.TitleText
import com.gatigagae.app.ui.components.TopBar
import com.gatigagae.app.ui.theme.GatigagaeTheme
import com.gatigagae.app.ui.theme.GgBackground
import com.gatigagae.app.ui.theme.GgBlue

/**
 * S2-2 프로필 등록·수정
 * 와이어프레임이 아직 없어서 명세서 항목만 넣은 기본 화면이에요. 와프 나오면 배치만 바꾸면 돼요.
 *
 * TODO: 지금은 앱을 껐다 켜면 프로필이 사라져요. 나중에 DataStore로 저장하기
 * TODO(기획 결정): 견종·나이·특이사항을 쓸지, "크기"를 추가할지
 */
@Composable
fun ProfileScreen(
    initial: DogProfile?,
    gear: Gear,
    onSave: (DogProfile) -> Unit,
    onCancel: () -> Unit,
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var breed by remember { mutableStateOf(initial?.breed ?: "") }
    var age by remember { mutableStateOf(initial?.age ?: "") }
    var note by remember { mutableStateOf(initial?.note ?: "") }
    var temp by remember { mutableIntStateOf(initial?.preferredTemp ?: 23) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GgBackground)
    ) {
        TopBar(gear = gear, dogMode = false)

        if (gear == Gear.D) {
            // S2-2-07: 주행 중에는 입력 막기
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                TitleText("주행 중에는 입력할 수 없어요")
                Spacer(Modifier.height(8.dp))
                SubText("정차 후 입력해 주세요", fontSize = 18.sp)
                Spacer(Modifier.height(24.dp))
                SecondaryButton("돌아가기", onClick = onCancel, modifier = Modifier.width(240.dp))
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.TopCenter,
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 900.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    TitleText(if (initial == null) "반려견 프로필 등록" else "반려견 프로필 수정")

                    ProfileField("이름 (필수)", name) { name = it }  // S2-2-01
                    ProfileField("견종", breed) { breed = it }       // S2-2-02
                    ProfileField("나이", age) { age = it }           // S2-2-03
                    ProfileField("특이사항", note, singleLine = false) { note = it } // S2-2-04

                    // 반려견 설정 온도 (S2-05에서 "어디서 바꿀지" 팀 결정 → 일단 여기에 둠)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("설정 온도", fontSize = 20.sp, modifier = Modifier.weight(1f))
                        SecondaryButton("－", onClick = { if (temp > 16) temp -= 1 }, modifier = Modifier.width(94.dp))
                        Text(
                            text = "${temp}℃",
                            color = GgBlue,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 24.dp),
                        )
                        SecondaryButton("＋", onClick = { if (temp < 30) temp += 1 }, modifier = Modifier.width(94.dp))
                    }

                    Spacer(Modifier.height(8.dp))
                }
            }

            // 하단 버튼 (S2-2-05, S2-2-06)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SecondaryButton("취소", onClick = onCancel, modifier = Modifier.weight(1f))
                PrimaryButton(
                    text = "저장",
                    enabled = name.isNotBlank(), // 이름 없으면 저장 불가
                    onClick = {
                        onSave(
                            DogProfile(
                                name = name.trim(),
                                breed = breed.trim(),
                                age = age.trim(),
                                note = note.trim(),
                                preferredTemp = temp,
                            )
                        )
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ProfileField(
    label: String,
    value: String,
    singleLine: Boolean = true,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        textStyle = TextStyle(fontSize = 20.sp),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Preview(widthDp = 1700, heightDp = 1184)
@Composable
private fun ProfileScreenPreview() {
    GatigagaeTheme {
        ProfileScreen(initial = FakeData.profile, gear = Gear.P, onSave = {}, onCancel = {})
    }
}
