/*
    Copyright 2023 George Lasry & CrypTool 2 Team

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package org.cryptool.ota;

import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Selection {
    private static Set<String> selectedIds = new HashSet<>();

    public static boolean isEmpty(){
        return selectedIds.isEmpty();
    }
    public static ArrayList<String> selectedIds() {
        return new ArrayList<>(selectedIds);
    }
    public static boolean contains(String id) {
        return selectedIds.contains(id);
    }

    public static int size(){
        return selectedIds().size();
    }

    public static String getFirst(){
        if (!selectedIds.isEmpty()) {
            return new ArrayList<>(selectedIds).get(0);
        }
        return null;
    }

    public static void add(String id){
        selectedIds.add(id);
    }
    public static String clear() {

        String previousFirst = getFirst();
        selectedIds.clear();

        return previousFirst;
    }

    public static Rectangle singleSelectedIdToRectangle() {
        if (size() != 1) {
            return null;
        }
        return TranscribedImage.idToRectangle(getFirst());
    }

    public static int singleSelectedIdToIndex() {
        if (size() != 1) {
            return -1;
        }
        return TranscribedImage.idToIndex(getFirst());
    }

    public static void toggleSelection(String id){
        if (selectedIds.contains(id)) {
            selectedIds.remove(id);
        } else {
            selectedIds.add(id);
        }
    }
}
