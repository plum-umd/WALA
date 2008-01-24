/******************************************************************************
 * Copyright (c) 2002 - 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *****************************************************************************/
package com.ibm.wala.cast.loader;

import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.ibm.wala.cast.tree.CAstSourcePositionMap;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IClassLoader;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.shrikeCT.ClassConstants;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.strings.Atom;

abstract public class AstClass implements IClass, ClassConstants {
  private final CAstSourcePositionMap.Position sourcePosition;

  private final TypeName typeName;

  private final IClassLoader loader;

  private final short modifiers;

  protected final Map<Atom, IField> declaredFields;

  protected final Map<Selector, IMethod> declaredMethods;

  protected AstClass(CAstSourcePositionMap.Position sourcePosition, TypeName typeName, IClassLoader loader, short modifiers,
      Map<Atom, IField> declaredFields, Map<Selector, IMethod> declaredMethods) {
    this.sourcePosition = sourcePosition;
    this.typeName = typeName;
    this.loader = loader;
    this.modifiers = modifiers;
    this.declaredFields = declaredFields;
    this.declaredMethods = declaredMethods;
  }

  public boolean isInterface() {
    return (modifiers & ACC_INTERFACE) != 0;
  }

  public boolean isAbstract() {
    return (modifiers & ACC_ABSTRACT) != 0;
  }

  public boolean isPublic() {
    return (modifiers & ACC_PUBLIC) != 0;
  }
  
  public boolean isStatic() {
    return (modifiers & ACC_STATIC) != 0;
  }

  public boolean isReferenceType() {
    return true;
  }

  public boolean isArrayClass() {
    return false;
  }

  public int getModifiers() {
    return modifiers;
  }

  public CAstSourcePositionMap.Position getSourcePosition() {
    return sourcePosition;
  }

  public URL getSourceURL() {
    return sourcePosition.getURL();
  }

  public String getSourceFileName() {
    return sourcePosition.getURL().getFile();
  }

  public TypeName getName() {
    return typeName;
  }

  public TypeReference getReference() {
    return TypeReference.findOrCreate(loader.getReference(), typeName);
  }

  public IClassLoader getClassLoader() {
    return loader;
  }

  public abstract IClass getSuperclass() throws ClassHierarchyException;

  private Collection<IClass> gatherInterfaces() throws ClassHierarchyException {
    Set<IClass> result = HashSetFactory.make();
    result.addAll(getDirectInterfaces());
    if (getSuperclass() != null)
      result.addAll(getSuperclass().getAllImplementedInterfaces());
    return result;
  }

  public abstract Collection<IClass> getDirectInterfaces() throws ClassHierarchyException;

  public Collection<IClass> getAllImplementedInterfaces() throws ClassHierarchyException {
    return gatherInterfaces();
  }

  public IMethod getClassInitializer() {
    return getMethod(MethodReference.clinitSelector);
  }

  public IMethod getMethod(Selector selector) {
    try {
      if (declaredMethods.containsKey(selector)) {
        return declaredMethods.get(selector);
      } else if (getSuperclass() != null) {
        return getSuperclass().getMethod(selector);
      } else {
        return null;
      }
    } catch (ClassHierarchyException e) {
      Assertions.UNREACHABLE();
      return null;
    }
  }

  public IField getField(Atom name) {
    try {
      if (declaredFields.containsKey(name)) {
        return declaredFields.get(name);
      } else if (getSuperclass() != null) {
        return getSuperclass().getField(name);
      } else {
        return null;
      }
    } catch (ClassHierarchyException e) {
      Assertions.UNREACHABLE();
      return null;
    }
  }

  public Collection<IMethod> getDeclaredMethods() {
    return declaredMethods.values();
  }

  public Collection<IField> getDeclaredInstanceFields() {
    Set<IField> result = HashSetFactory.make();
    for (Iterator<IField> FS = declaredFields.values().iterator(); FS.hasNext();) {
      IField F = FS.next();
      if (!F.isStatic()) {
        result.add(F);
      }
    }

    return result;
  }

  public Collection<IField> getDeclaredStaticFields() {
    Set<IField> result = HashSetFactory.make();
    for (Iterator<IField> FS = declaredFields.values().iterator(); FS.hasNext();) {
      IField F = FS.next();
      if (F.isStatic()) {
        result.add(F);
      }
    }

    return result;
  }

  public Collection<IField> getAllInstanceFields() throws ClassHierarchyException {
    Collection<IField> result = HashSetFactory.make();
    result.addAll(getDeclaredInstanceFields());
    if (getSuperclass() != null) {
      result.addAll(getSuperclass().getAllInstanceFields());
    }

    return result;
  }

  public Collection<IField> getAllStaticFields() throws ClassHierarchyException {
    Collection<IField> result = HashSetFactory.make();
    result.addAll(getDeclaredStaticFields());
    if (getSuperclass() != null) {
      result.addAll(getSuperclass().getAllStaticFields());
    }

    return result;
  }

  public Collection<IField> getAllFields() throws ClassHierarchyException {
    Collection<IField> result = HashSetFactory.make();
    result.addAll(getAllInstanceFields());
    result.addAll(getAllStaticFields());
    return result;
  }

  public Collection<IMethod> getAllMethods() throws ClassHierarchyException {
    Collection<IMethod> result = HashSetFactory.make();
    for (Iterator<IMethod> ms = getDeclaredMethods().iterator(); ms.hasNext();) {
      result.add(ms.next());
    }
    if (getSuperclass() != null) {
      result.addAll(getSuperclass().getAllMethods());
    }

    return result;
  }

}
