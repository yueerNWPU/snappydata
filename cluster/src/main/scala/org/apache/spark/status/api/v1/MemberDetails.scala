/*
 * Changes for SnappyData data platform.
 *
 * Portions Copyright (c) 2017 SnappyData, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License. See accompanying
 * LICENSE file.
 */
package org.apache.spark.status.api.v1

import scala.collection.mutable.ListBuffer
import scala.util.control._

import com.pivotal.gemfirexd.internal.engine.ui.MemberStatistics
import io.snappydata.SnappyTableStatsProviderService

object MemberDetails {

  def getAllMembersInfo: Seq[MemberSummary] = {
    val allMembers = SnappyTableStatsProviderService.getService.getMembersStatsFromService
    val membersBuff: ListBuffer[MemberSummary] = ListBuffer.empty[MemberSummary]

    allMembers.foreach(mem => {
      val memberDetails = mem._2

      val ms = getMemberSummary(memberDetails)

      membersBuff += ms

    })

    membersBuff.toList
  }

  def getMembersInfo(memId: String): Seq[MemberSummary] = {
    val allMembers = SnappyTableStatsProviderService.getService.getMembersStatsFromService
    val membersBuff: ListBuffer[MemberSummary] = ListBuffer.empty[MemberSummary]

    val loop = new Breaks;
    loop.breakable {
      allMembers.foreach(mem => {
        val memberDetails = mem._2

        if (memberDetails.getId.equalsIgnoreCase(memId)) {

          val ms = getMemberSummary(memberDetails)

          membersBuff += ms

          loop.break;
        }

      })
    }

    membersBuff.toList
  }

  def getMemberSummary(memberDetails: MemberStatistics): MemberSummary = {

    val status = memberDetails.getStatus
    /*
    val statusImgUri = if (status.toString.toLowerCase.equals("running")) {
      "/static/snappydata/running-status-icon-20x19.png"
    } else {
      "/static/snappydata/stopped-status-icon-20x19.png"
    }
    */

    val memberId = memberDetails.getId

    val nameOrId = {
      if (memberDetails.getName.isEmpty
          || memberDetails.getName.equalsIgnoreCase("NA")) {
        memberDetails.getId
      } else {
        memberDetails.getName
      }
    }

    val host = memberDetails.getHost
    val fullDirName = memberDetails.getUserDir
    val shortDirName = fullDirName.substring(
      fullDirName.lastIndexOf(System.getProperty("file.separator")) + 1)
    val logFile = memberDetails.getLogFile
    val processId = memberDetails.getProcessId

    // val distStoreUUID = memberDetails.getDiskStoreUUID
    // val distStoreName = memberDetails.getDiskStoreName

    val isLead: Boolean = memberDetails.isLead
    val isActiveLead: Boolean = memberDetails.isLeadActive
    val isLocator: Boolean = memberDetails.isLocator
    val isDataServer: Boolean = memberDetails.isDataServer
    val memberType = {
      if (isLead || isActiveLead) {
        "LEAD"
      } else if (isLocator) {
        "LOCATOR"
      } else if (isDataServer) {
        "DATA SERVER"
      } else {
        "CONNECTOR"
      }
    }

    val cpuActive = memberDetails.getCpuActive
    val clients = memberDetails.getClientsCount

    val heapStoragePoolUsed = memberDetails.getHeapStoragePoolUsed
    val heapStoragePoolSize = memberDetails.getHeapStoragePoolSize
    val heapExecutionPoolUsed = memberDetails.getHeapExecutionPoolUsed
    val heapExecutionPoolSize = memberDetails.getHeapExecutionPoolSize

    val offHeapStoragePoolUsed = memberDetails.getOffHeapStoragePoolUsed
    val offHeapStoragePoolSize = memberDetails.getOffHeapStoragePoolSize
    val offHeapExecutionPoolUsed = memberDetails.getOffHeapExecutionPoolUsed
    val offHeapExecutionPoolSize = memberDetails.getOffHeapExecutionPoolSize

    val heapMemorySize = memberDetails.getHeapMemorySize
    val heapMemoryUsed = memberDetails.getHeapMemoryUsed
    val offHeapMemorySize = memberDetails.getOffHeapMemorySize
    val offHeapMemoryUsed = memberDetails.getOffHeapMemoryUsed


    val jvmHeapMax = memberDetails.getJvmMaxMemory
    val jvmHeapTotal = memberDetails.getJvmTotalMemory
    val jvmHeapUsed = memberDetails.getJvmUsedMemory
    val jvmHeapFree = memberDetails.getJvmFreeMemory

    val timeLine = memberDetails.getUsageTrends(MemberStatistics.TREND_TIMELINE)
    val cpuUsageTrends = memberDetails.getUsageTrends(MemberStatistics.TREND_CPU_USAGE)
    val jvmUsageTrends = memberDetails.getUsageTrends(MemberStatistics.TREND_JVM_HEAP_USAGE)
    val heapUsageTrends = memberDetails.getUsageTrends(MemberStatistics.TREND_HEAP_USAGE)
    val heapStorageUsageTrends = memberDetails.getUsageTrends(
      MemberStatistics.TREND_HEAP_STORAGE_USAGE)
    val heapExecutionUsageTrends = memberDetails.getUsageTrends(
      MemberStatistics.TREND_HEAP_EXECUTION_USAGE)
    val offHeapUsageTrends = memberDetails.getUsageTrends(MemberStatistics.TREND_OFFHEAP_USAGE)
    val offHeapStorageUsageTrends = memberDetails.getUsageTrends(
      MemberStatistics.TREND_OFFHEAP_STORAGE_USAGE)
    val offHeapExecutionUsageTrends = memberDetails.getUsageTrends(
      MemberStatistics.TREND_OFFHEAP_EXECUTION_USAGE)
    val aggrMemoryUsageTrends = memberDetails.getUsageTrends(
      MemberStatistics.TREND_AGGR_MEMORY_USAGE)

    new MemberSummary(memberId, nameOrId.toString, host, shortDirName, fullDirName,
      logFile, processId, status, memberType, isLocator, isDataServer, isLead, isActiveLead,
      cpuActive, clients, jvmHeapMax, jvmHeapUsed, jvmHeapTotal, jvmHeapFree,
      heapStoragePoolUsed, heapStoragePoolSize, heapExecutionPoolUsed, heapExecutionPoolSize,
      heapMemorySize, heapMemoryUsed, offHeapStoragePoolUsed, offHeapStoragePoolSize,
      offHeapExecutionPoolUsed, offHeapExecutionPoolSize, offHeapMemorySize, offHeapMemoryUsed,
      timeLine, cpuUsageTrends, jvmUsageTrends, heapUsageTrends, heapStorageUsageTrends,
      heapExecutionUsageTrends, offHeapUsageTrends, offHeapStorageUsageTrends,
      offHeapExecutionUsageTrends, aggrMemoryUsageTrends)
  }

}