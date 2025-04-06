package com.github.compilebulai.hack.toolWindow

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object TaskGenerator {
    private val client = OkHttpClient()
    private const val OPENAI_URL = "";
    private const val OPENAI_MODEL = "gpt-3.5-turbo"

    // ⚠️ Hardcoded key (ideally, move it to .env or an environment variable)
    private const val OPENAI_TOKEN = " "

    suspend fun generateTasks(prompt: String, shortMode: Boolean): List<String> = withContext(Dispatchers.IO) {
        val stylePrompt = if (shortMode) {
            "Generate only very short tasks, one per line (maximum 5 words)."
        } else {
            "Generate detailed, concrete tasks, each starting with a verb (e.g., 'Create', 'Add'), without explanations."
        }

        val fullPrompt = """
            $prompt

            $stylePrompt
            Return only the list. Do not add any additional commentary.
        """.trimIndent()

        val json = JSONObject().apply {
            put("model", OPENAI_MODEL)
            put("messages", listOf(
                mapOf("role" to "user", "content" to fullPrompt)
            ))
        }

        val request = Request.Builder()
            .url(OPENAI_URL)
            .addHeader("Authorization", "Bearer $OPENAI_TOKEN")
            .addHeader("Content-Type", "application/json")
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: return@withContext emptyList()

        val choices = JSONObject(body).optJSONArray("choices") ?: return@withContext emptyList()
        if (choices.length() == 0) return@withContext emptyList()

        val message = choices.getJSONObject(0).getJSONObject("message").getString("content")

        return@withContext message
            .lines()
            .mapNotNull {
                it.trim()
                    .removePrefix("-")
                    .replace(Regex("^\\d+[.)]?\\s*"), "")
                    .takeIf { line -> line.isNotBlank() }
            }
    }
    suspend fun generateDescriptionForTask(taskTitle: String): String = withContext(Dispatchers.IO) {
        val descriptionPrompt = """
            You have the following task: "$taskTitle".
            Generate a brief implementation note or summary for how to accomplish it, in English.
            Return only the text, do not add bullet points, disclaimers or extra commentary.
        """.trimIndent()

        val json = JSONObject().apply {
            put("model", OPENAI_MODEL)
            put("messages", listOf(
                mapOf("role" to "user", "content" to descriptionPrompt)
            ))
        }

        val request = Request.Builder()
            .url(OPENAI_URL)
            .addHeader("Authorization", "Bearer $OPENAI_TOKEN")
            .addHeader("Content-Type", "application/json")
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: return@withContext ""

        val choices = JSONObject(body).optJSONArray("choices") ?: return@withContext ""
        if (choices.length() == 0) return@withContext ""

        return@withContext choices.getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
            .trim()
    }


}
