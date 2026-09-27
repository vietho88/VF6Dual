package com.vf6.dual.bridge;

import android.os.Bundle;

interface IVF6PrivilegedService {
    String exec(String command);
    Bundle mirrorDisplay(int displayId);
    void releaseMirror(in Bundle mirrorBundle);
    void destroy();
}
