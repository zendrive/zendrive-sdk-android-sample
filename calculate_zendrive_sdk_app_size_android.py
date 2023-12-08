import argparse
import fileinput
import re
import subprocess
import os


# Check Java version is set as 11
def check_java_version():
    # Run the 'java -version' command and capture the output
    try:
        java_version_output = subprocess.check_output(['java', '-version'], stderr=subprocess.STDOUT,
                                                      universal_newlines=True)
    except subprocess.CalledProcessError as e:
        java_version_output = e.output

    # Check if the output contains the desired version
    if "version \"11" in java_version_output:
        print("Java version 11 is set.")
    else:
        print("Java version is not set to 11 or Java may not be installed.")


def build_and_bundle_android_project():
    print("\tClean android project")
    # Clean the project
    clean_command = "./gradlew clean"
    subprocess.run(clean_command.split())
    print("\tClean successful")

    print("\tBuild android project")
    # Build the project
    build_command = "./gradlew assembleDebug"
    subprocess.run(build_command.split())
    print("\tBuild successful")

    print("\tBundle android project")
    # Bundle the project
    build_command = "./gradlew bundle"
    subprocess.run(build_command.split())
    print("\tBundle successful")


def get_min_max_sample_app_size(sdk_android_sample_project_path, release_type):
    os.chdir(sdk_android_sample_project_path)
    os.chdir(f"app/build/outputs/bundle/{release_type}")

    print("\tBuild apk using bundle tool")
    # Build the project
    build_command = f"java -jar {sdk_android_sample_project_path}/bundletool-all-1.15.4.jar build-apks --bundle=app-{release_type}.aab --output=my_app.apks"
    subprocess.run(build_command.split())
    print("\tApk builded successfully")

    # Build the project
    build_command = f"java -jar {sdk_android_sample_project_path}/bundletool-all-1.15.4.jar get-size total --apks=my_app.apks"
    completed_process = subprocess.run(build_command.split(), stdout=subprocess.PIPE)
    process_output = completed_process.stdout.decode('utf-8')
    # Split the lines of the output
    lines = process_output.strip().split('\n')
    # Split the header and data
    min_app_size, max_app_size = lines[1].split(",")
    return min_app_size, max_app_size


# Get sample app size
def get_release_debug_min_max_sample_app_size(sdk_android_sample_project_path):
    build_and_bundle_android_project()
    release_min_app_size, release_max_app_size = get_min_max_sample_app_size(sdk_android_sample_project_path, "release")
    debug_min_app_size, debug_max_app_size = get_min_max_sample_app_size(sdk_android_sample_project_path, "debug")
    app_size = {
        "release_min": int(release_min_app_size)/1000000,
        "release_max": int(release_max_app_size)/1000000,
        "debug_min": int(debug_min_app_size)/1000000,
        "debug_max": int(debug_max_app_size)/1000000
    }
    return app_size


def update_build_gradle_variable(sdk_android_sample_project_path, file_path, variable_name, new_value):
    # Change the working directory to the root directory of your Android project
    os.chdir(sdk_android_sample_project_path)
    updated = False
    # Regular expression pattern to match the variable assignment
    pattern = re.compile(rf'\s*def {variable_name}\s*=\s*[\'"].*?[\'"]')

    # Iterate through each line in the file
    for line in fileinput.input(file_path, inplace=True):
        # Check if the line contains the variable assignment
        if re.match(pattern, line):
            # Update the variable value
            line = f'    def {variable_name} = \'{new_value}\'\n'
            updated = True
        # Output the line (with modifications, if any)
        print(line, end='')

    # Check if the variable was updated
    if updated:
        print(f"Updated '{variable_name}' to '{new_value}' in {file_path}")
    else:
        print(f"Variable '{variable_name}' not found in {file_path}")


def update_dependency_name(sdk_android_sample_project_path, file_path, old_dependency_name, new_dependency_name):
    os.chdir(sdk_android_sample_project_path)
    updated = False
    # Regular expression pattern to match the dependency with the specified name
    pattern = re.compile(rf'implementation\s+"[^"]*:{old_dependency_name}:\$[a-zA-Z_][a-zA-Z0-9_]*"')

    # Iterate through each line in the file
    for line in fileinput.input(file_path, inplace=True):
        # Check if the line contains the dependency with the specified name
        if re.search(pattern, line):
            # Update the dependency name
            line = re.sub(rf'(:{old_dependency_name}:\$[a-zA-Z_][a-zA-Z0-9_]*)', f':{new_dependency_name}:$zendrive_sdk_version', line)
            updated = True
        # Output the line (with modifications, if any)
        print(line, end='')

    # Check if the dependency was updated
    if updated:
        print(f"Updated dependency name '{old_dependency_name}' to '{new_dependency_name}' in {file_path}")
    else:
        print(f"Dependency '{old_dependency_name}' not found in {file_path}")


def main(args):
    sample_app_without_zendrive_sdk = {
        "release_min": 0.64,
        "release_max": 0.67,
        "debug_min": 1.69,
        "debug_max": 1.72
    }

    check_java_version()
    build_file = 'app/build.gradle'
    sdk_android_sample_project_absolute_path = os.path.abspath(args.sdk_android_sample_project_path)

    # GMS Build
    update_build_gradle_variable(sdk_android_sample_project_absolute_path, build_file, "zendrive_sdk_version", args.zendrive_sdk_version)
    sample_app_with_zendrive_sdk_gms = get_release_debug_min_max_sample_app_size(sdk_android_sample_project_absolute_path)

    # HMS Build
    update_dependency_name(sdk_android_sample_project_absolute_path, build_file, "ZendriveSDK", "ZendriveSDK-HMS")
    sample_app_with_zendrive_sdk_hms = get_release_debug_min_max_sample_app_size(sdk_android_sample_project_absolute_path)

    # To verify the build version
    file_path = f"{sdk_android_sample_project_absolute_path}/app/build.gradle"

    # Open the file
    with open(file_path, 'r') as file:
        for line in file:
            if line.lstrip().startswith('def zendrive_sdk_version'):
                build_number = line.split("'")[1]
                print("Build number for Zendrive SDK Android : ", build_number)
                break

    print("\n")
    for key, value in sample_app_with_zendrive_sdk_gms.items():
        print(f"GMS {key} value : {value:.2f}")

    print("\n")
    for key, value in sample_app_with_zendrive_sdk_gms.items():
        print(f"GMS size increase due to {key} value : {(value - sample_app_without_zendrive_sdk[key]):.2f}")

    print("\n")
    for key, value in sample_app_with_zendrive_sdk_hms.items():
        print(f"HMS {key} value : {value:.2f}")

    print("\n")
    for key, value in sample_app_with_zendrive_sdk_hms.items():
        print(f"HMS size increase due to {key} value : {(value - sample_app_without_zendrive_sdk[key]):.2f}")


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('-v', '--zendrive_sdk_version', dest='zendrive_sdk_version', required=True, help="""Version of zendrive_sdk""")
    parser.add_argument('-p', '--sdk_android_sample_project_path', dest='sdk_android_sample_project_path', required=True, help="""Path for sample app project""")
    main(args=parser.parse_args())
