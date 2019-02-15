# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)


include_directories(glsl-optimizer/include)
include_directories(glsl-optimizer/src/mesa)
include_directories(glsl-optimizer/src/mapi)
include_directories(glsl-optimizer/src/glsl)
include_directories(glsl-optimizer/src)

option (DEBUG "Enable debugging" FALSE)

if(${DEBUG} MATCHES "on")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -g -O0")
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -g -O0")
else()
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Os -DNDEBUG")
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Os -DNDEBUG")
    set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -s")
endif()


file(GLOB glcpp-library_sources ./glsl-optimizer/src/glsl/glcpp/*.c ./glsl-optimizer/src/util/*.c)
#file(GLOB glcpp-library_sources_remove src/glsl/glcpp/glcpp.c)
#list(REMOVE_ITEM glcpp-library_sources ${glcpp-library_sources_remove})
add_library(glcpp-library ${glcpp-library_sources})

file(GLOB mesa_sources ./glsl-optimizer/src/mesa/program/*.c ./glsl-optimizer/src/mesa/main/*.c)
add_library(mesa ${mesa_sources})

file(GLOB glsl_sources ./glsl-optimizer/src/glsl/*.cpp ./glsl-optimizer/src/glsl/*.c)
file(GLOB glsl_sources_remove ./glsl-optimizer/src/glsl/main.cpp ./glsl-optimizer/src/glsl/builtin_stubs.cpp)
list(REMOVE_ITEM glsl_sources ${glsl_sources_remove})
add_library(glsl_optimizer SHARED ${glsl_sources})
target_link_libraries(glsl_optimizer glcpp-library mesa)

add_executable(glsl_compiler ./glsl-optimizer/src/glsl/main.cpp)
target_link_libraries(glsl_compiler glsl_optimizer)

file(GLOB glsl_test_sources ./glsl-optimizer/tests/*.cpp)
add_executable(glsl_test ${glsl_test_sources})
target_link_libraries(glsl_test glsl_optimizer)

find_library(OpenGL_LIBRARY GLESv3)
target_link_libraries(glsl_test ${OpenGL_LIBRARY})

file(GLOB glslopt_sources ./glsl-optimizer/contrib/glslopt/*.cpp)
add_executable(glslopt ${glslopt_sources})
target_link_libraries(glslopt glsl_optimizer)

#add_library(glsl_optimizer SHARED ./glsl-optimizer/src/glsl/glsl_optimizer.cpp)


# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
add_library( # Sets the name of the library.
        jni_optimizer

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        jni_optimizer.cpp)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.
find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)


# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        jni_optimizer

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})

target_link_libraries( # Specifies the target library.
        jni_optimizer

        # Links the target library to the log library
        # included in the NDK.
        glsl_optimizer)

