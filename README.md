# 🧠 Crispy – AI-Enhanced Project Task Manager for IntelliJ

**Crispy** is a smart, interactive, and AI-powered task management plugin designed to help developers organize, plan, and track tasks directly inside IntelliJ IDEA.

Whether you're starting a new project, breaking down a feature, or just keeping track of coding goals, Crispy helps you stay focused and efficient.

---

## 🚀 Features

- ✅ **To-Do List Tool Window** integrated in the IDE
- 🤖 **AI-powered task generator** using OpenAI
- 📝 **Editable notes** for each task
- 🔗 **Link code to task** and navigate directly
- 📍 **Persistent task storage per project**
- 🐱 **Cat GIF reward** when you complete all tasks
- 📊 **Progress bar tracking completion**
- 🧠 **Smart duplicate detection**
- 🔒 **Project-level state saved in XML**

---

## 🔧 How It Works

1. On first plugin load, Crispy asks:  
   _“What project do you want to implement?”_

2. You enter a short description — e.g.  
   _“A weather app with a REST API and UI”_

3. Then, you choose:
   - **Short tasks** ("Build UI", "Connect API")
   - or **Detailed tasks** ("Design weather forecast screen", etc.)

4. Tasks are generated via **OpenAI** and added directly to the list.

5. For each task, you can:
   - Add/edit notes (`📝 Note` button)
   - Link selected code from the editor (`Right click → Associate Code with Task`)
   - Jump to code location (`GoTo Code` button)

---

## 📁 File Structure

- `ToDoList.kt` – Main tool window logic
- `Task.kt` – Data model for tasks
- `ToDoListService.kt` – Persistence and storage logic
- `TaskWithNotePanel.kt` – UI component per task
- `plugin.xml` – Plugin definition and extensions
- `TaskGenerator.kt` – AI integration via OpenAI API
- `/cat/cat.gif` – The reward 🐱

---

## 🧠 Powered by AI

Crispy uses OpenAI's ChatGPT (via API) to:

- Transform project descriptions into action-oriented task lists
- Optionally generate short vs. detailed task styles
- Enhance productivity through automatic breakdown

> 🔐 The OpenAI API key is securely used and never exposed in the UI.

---

## ⚙️ Installation & Setup

1. Clone the repo:
   ```bash
   git clone https://github.com/AndreiBug/HackItAll.git
   cd HackItAll
