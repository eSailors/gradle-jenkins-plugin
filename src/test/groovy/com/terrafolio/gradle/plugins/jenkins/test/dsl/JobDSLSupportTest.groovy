package com.terrafolio.gradle.plugins.jenkins.test.dsl

import com.terrafolio.gradle.plugins.jenkins.dsl.JobDSLSupport
import com.terrafolio.gradle.plugins.jenkins.jobdsl.MapJobManagement
import com.terrafolio.gradle.plugins.jenkins.test.TempDirSpec
import javaposse.jobdsl.dsl.JobManagement
import org.custommonkey.xmlunit.Diff
import org.custommonkey.xmlunit.XMLUnit

/**
 * Created by ghale on 6/3/14.
 */
class JobDSLSupportTest extends TempDirSpec {
    def JobDSLSupport support
    def JobManagement mockJobManagement

    def setup() {
        mockJobManagement = Mock(JobManagement)
        support = new TestDSLSupport(mockJobManagement)
    }

    def "addConfig adds a config to JobManagement" () {
        when:
        support.addConfig("test", "XXX")

        then:
        1 * mockJobManagement.createOrUpdateConfig("test", "XXX", true)
    }

    def "getConfig returns a config from JobManagement" () {
        when:
        support.getConfig("test")

        then:
        1 * mockJobManagement.getConfig("test")
    }

    def "setParameter sets JobManagement parameter" () {
        setup:
        Map map = new HashMap<String, String>()

        when:
        support.setParameter("test", "value")

        then:
        1 * mockJobManagement.getParameters() >> { map }
        map.test == "value"
    }

    def "evaluateDSL from file creates correct XML" () {
        setup:
        File file = file("test.dsl",
                """
                    freeStyleJob("test") {
                    }
                """
        )
        support = new TestDSLSupport(new MapJobManagement(new HashMap<String, String>()))
        XMLUnit.setIgnoreWhitespace(true)

        expect:
        support.evaluateDSL(file) == 'test'
        new Diff(JobFixtures.FREEFORM_DSL_JOB_XML, support.getConfig('test')).similar()
    }

    def "evaluateDSL from closure creates correct XML" (String type, String expectedXml) {
        setup:
        Closure dsl = {
            name = "test"
        }
        support = new TestDSLSupport(new MapJobManagement(new HashMap<String, String>()))
        XMLUnit.setIgnoreWhitespace(true)

        expect:
        support.evaluateDSL('test', type, dsl) == 'test'
        new Diff(expectedXml, support.getConfig('test')).similar()

        where:
        type        | expectedXml
        'Freeform'  | JobFixtures.FREEFORM_DSL_JOB_XML
        'Maven'     | JobFixtures.MAVEN_DSL_JOB_XML
        'Multijob'  | JobFixtures.MULTIJOB_DSL_JOB_XML
        'Buildflow' | JobFixtures.BUILDFLOW_DSL_JOB_XML
    }

    class TestDSLSupport implements JobDSLSupport {
        final JobManagement jobManagement

        TestDSLSupport(JobManagement jobManagement) {
            this.jobManagement = jobManagement
        }
    }
}
