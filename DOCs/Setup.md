# BladeRunner Project Setup Guide: Cloning, Installing JDK, and Configuring JSON Library in VS Code

This guide will help you set up the BladeRunner project in VS Code, including steps to clone the repository, install the Java Development Kit (JDK), and configure the `json-20240303.jar` library for JSON handling.

---

## Step 1: Install the Java Development Kit (JDK)

Ensure that the JDK is installed on your system, as it’s required for compiling and running Java applications.

1. **Download and Install JDK**:
   - Visit the [Oracle JDK Download Page](https://www.oracle.com/java/technologies/javase-downloads.html) or [OpenJDK](https://openjdk.java.net/install/) for the open-source version.
   - Download the installer for your operating system (Windows, macOS, or Linux) and follow the instructions to install it.

2. **Verify the JDK Installation**:
   - Open your terminal or command prompt.
   - Run the following command to confirm JDK is installed:
     ```bash
     java -version
     ```
   - You should see an output showing the installed JDK version, such as:
     ```
     java version "17.0.1" 2021-10-19 LTS
     ```

3. **Set JAVA_HOME (Optional)**:
   - If needed, set the `JAVA_HOME` environment variable to point to your JDK installation directory:
     - On **Windows**: 
       - Open **System Properties > Environment Variables** and add a new system variable `JAVA_HOME` pointing to your JDK path (e.g., `C:\Program Files\Java\jdk-17`).
     - On **macOS/Linux**:
       - Add the following lines to `.bashrc`, `.zshrc`, or `.profile`:
         ```bash
         export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home
         export PATH=$JAVA_HOME/bin:$PATH
         ```

---

## Step 2: Clone the Repository

1. Open your terminal and run:
   ```bash
   git clone https://github.com/Wasif-ZA/BladeRunner.git
   ```

2. Change to the `CCP` directory:
   ```bash
   cd BladeRunner/CCP
   ```

---

## Step 3: Open VS Code and Install Java Extensions

1. **Open VS Code**.
2. Install the **Java Extension Pack**:
   - Go to the **Extensions** tab (`Ctrl + Shift + X`).
   - Search for **Java Extension Pack** and install it if not already installed. This includes support for Java projects, builds, debugging, and more.

---

## Step 4: Open the Project in VS Code

1. Go to **File > Open Folder**.
2. Select the `BladeRunner/CCP` directory.

---

## Step 5: Add the JSON JAR to the Classpath in VS Code

1. **Ensure the JAR File is in the Project**:
   - Make sure `json-20240303.jar` is located in the `lib` folder inside your project directory.

2. **Add the JAR File to the Classpath**:
   - In the **Java Projects** section on the left, click the **+** icon under **Referenced Libraries** to add external JAR files.
   - Navigate to the `lib` folder in your project and select `json-20240303.jar`.

---

## Step 6: Verify JSON Import in Code

1. Open `CCP.java` (or any Java file in your project) and add the following code to verify that `org.json.JSONObject` is recognized:

   ```java
   import org.json.JSONObject;

   public class TestJSON {
       public static void main(String[] args) {
           JSONObject jsonObj = new JSONObject();
           jsonObj.put("name", "Blade Runner");
           jsonObj.put("status", "active");
           System.out.println(jsonObj.toString());
       }
   }
   ```

2. **Run the Program**:
   - Press `F5` to run the program. You should see the following output in the terminal:
     ```json
     {"name":"Blade Runner","status":"active"}
     ```

---

## Step 7: Rebuild the Project (Optional)

1. **Rebuild the Project**:
   - If you encounter issues, use the **Java: Clean Java Language Server Workspace** command from the VS Code Command Palette (`Ctrl + Shift + P`) to refresh the Java Language Server and rebuild the workspace.
   
---

## Additional Tips

- Ensure the correct **Java SDK** is configured. You can check this by going to **Settings** (`Ctrl + ,`) and searching for `java.home`.
- If debugging or running code causes issues, verify that the correct **Java Runtime** is selected in the **Java Projects** section.

---

By following these steps, you will successfully clone the repository, add the `json-20240303.jar` file to the classpath, and verify that the `JSONObject` class is properly recognized in your VS Code Java project.